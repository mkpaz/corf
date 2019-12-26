package org.telekit.base.net;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.telekit.base.net.HttpConstants.AuthScheme;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.telekit.base.net.HttpConstants.Method;
import static org.telekit.base.util.NumberUtils.ensureRange;

public class ApacheHttpClient implements HttpClient {

    public static final Set<String> SUPPORTED_SSL_PROTOCOLS = HttpConstants.SSL_PROTOCOLS;

    private final ResponseHandler<Response> handler = new SpecificResponseHandler();
    private final CloseableHttpClient client;
    private final HttpClientContext localContext;

    private ApacheHttpClient(CloseableHttpClient client, HttpClientContext localContext) {
        this.client = client;
        this.localContext = localContext;
    }

    @Override
    public Response execute(Request request) {
        try {
            HttpRequestBase httpRequest = createHttpRequest(request);
            Response response = client.execute(httpRequest, handler, localContext);

            // extract actual request headers from context
            request.headers().putAll(
                    headersToMap(localContext.getRequest().getAllHeaders())
            );
            return response;
        } catch (IOException e) {
            return new Response(-1, e.getMessage(), new HashMap<>(), getStackTrace(getRootCause(e)));
        }
    }

    private static Header[] mapToHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(e -> new BasicHeader(e.getKey(), e.getValue()))
                .toArray(Header[]::new);
    }

    private static SortedMap<String, String> headersToMap(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue, (v1, v2) -> v2, TreeMap::new));
    }

    private static String consumeBodyQuietly(HttpEntity entity) {
        try {
            return entity != null ? EntityUtils.toString(entity) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return "<unable to obtain response body>";
        }
    }

    public HttpRequestBase createHttpRequest(Request request) {
        HttpRequestBase requestBase = switch (request.method()) {
            case DELETE -> new ApacheHttpClient.HttpDelete();
            case GET -> new HttpGet();
            case HEAD -> new HttpHead();
            case PATCH -> new HttpPatch();
            case POST -> new HttpPost();
            case PUT -> new HttpPut();
        };

        requestBase.setURI(request.uri());
        requestBase.setHeaders(mapToHeaders(request.headers()));

        if (requestBase instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) requestBase).setEntity(new ByteArrayEntity(request.body().getBytes()));
        }

        return requestBase;
    }

    public static Builder builder() {
        return new Builder();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Builder {

        private final HttpClientBuilder httpBuilder = HttpClients.custom();
        private final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        private final HttpClientContext localContext = HttpClientContext.create();

        public Builder() {
            requestConfigBuilder
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setSocketTimeout(RESPONSE_TIMEOUT);
            httpBuilder.setUserAgent(USER_AGENT);
        }

        public Builder timeouts(int timeout) {
            return timeouts(timeout, timeout);
        }

        public Builder timeouts(int connectTimeout, int responseTimeout) {
            // the time to establish the connection with the remote host
            requestConfigBuilder.setConnectTimeout(
                    ensureRange(connectTimeout, 100, (int) TimeUnit.MINUTES.toMillis(5))
            );
            // the time waiting for data after establishing the connection
            // (maximum time of inactivity between two data packets)
            requestConfigBuilder.setSocketTimeout(
                    ensureRange(responseTimeout, 100, (int) TimeUnit.MINUTES.toMillis(5))
            );
            return this;
        }

        public Builder userAgent(String userAgent) {
            httpBuilder.setUserAgent(userAgent);
            return this;
        }

        public Builder trustAllCertificates() {
            try {
                SSLContext sslContext = SSLContexts.custom()
                        .loadTrustMaterial((chain, authType) -> true)
                        .build();

                // ignore self-signed certificates (https://stackoverflow.com/q/1828775/7421700)
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                        sslContext,
                        SUPPORTED_SSL_PROTOCOLS.toArray(new String[0]),
                        null,
                        NoopHostnameVerifier.INSTANCE
                );

                httpBuilder.setSSLSocketFactory(sslSocketFactory);
            } catch (Exception ignored) {}

            return this;
        }

        public Builder ignoreCookies() {
            requestConfigBuilder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
            return this;
        }

        public Builder proxy(URI uri, PasswordAuthentication auth) {
            HttpHost proxy = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            // credentials is optional for proxy
            if (auth != null) {
                credentialsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(auth.getUserName(), new String(auth.getPassword()))
                );
            }
            httpBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
            return this;
        }

        public Builder basicAuth(PasswordAuthentication auth, URI uri, boolean preemptive) {
            return passwordBasedAuth(AuthScheme.BASIC, auth, uri, preemptive);
        }

        public Builder digestAuth(PasswordAuthentication auth, URI uri, boolean preemptive) {
            return passwordBasedAuth(AuthScheme.DIGEST, auth, uri, preemptive);
        }

        private Builder passwordBasedAuth(AuthScheme authScheme,
                                          PasswordAuthentication auth,
                                          URI uri,
                                          boolean preemptive
        ) {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                    auth.getUserName(), new String(auth.getPassword())
            );
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            httpBuilder.setDefaultCredentialsProvider(credentialsProvider);

            // preemptive auth puts auth headers into each request to omit challenge (401) stage
            if (!preemptive) { return this; }

            // BasicAuthCache doesn't mean "for basic auth", it's just a misleading name choice
            AuthCache authCache = new BasicAuthCache();
            HttpHost targetHost = HttpHost.create(uri.getHost());

            if (authScheme == AuthScheme.BASIC) { authCache.put(targetHost, new BasicScheme()); }
            if (authScheme == AuthScheme.DIGEST) { authCache.put(targetHost, new DigestScheme()); }

            localContext.setAuthCache(authCache);

            return this;
        }

        public ApacheHttpClient build() {
            httpBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
            CloseableHttpClient httpClient = httpBuilder.build();
            return new ApacheHttpClient(httpClient, localContext);
        }
    }

    // DELETE method with body support
    public static class HttpDelete extends HttpEntityEnclosingRequestBase {

        public HttpDelete() {
            super();
        }

        public HttpDelete(URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return Method.DELETE.name();
        }
    }

    public static class SpecificResponseHandler implements ResponseHandler<Response> {

        @Override
        public Response handleResponse(HttpResponse response) {
            StatusLine statusLine = response.getStatusLine();
            return new Response(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase(),
                    headersToMap(response.getAllHeaders()),
                    consumeBodyQuietly(response.getEntity())
            );
        }
    }
}
