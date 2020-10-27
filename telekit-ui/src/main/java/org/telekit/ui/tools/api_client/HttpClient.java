package org.telekit.ui.tools.api_client;

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
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.telekit.base.domain.AuthPrincipal;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

public class HttpClient {

    @SuppressWarnings("SpellCheckingInspection")
    public static final String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; " +
            "Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; " +
            "Media Center PC 6.0; InfoPath.2)";
    public static final String CONTENT_TYPE_HEADER = HTTP.CONTENT_TYPE;

    public static final int CONNECT_TIMEOUT = 5000;
    public static final int DATA_TIMEOUT = 5000;

    private final HttpClientBuilder httpBuilder;
    private final ResponseHandler<Response> handler = new SpecificResponseHandler();
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private final HttpClientContext localContext = HttpClientContext.create();
    private CloseableHttpClient client;

    public HttpClient(int connectTimeout, int responseTimeout) {
        RequestConfig.Builder config = RequestConfig.custom()
                // the time to establish the connection with the remote host
                .setConnectTimeout(connectTimeout)
                // the time waiting for data after establishing the connection
                // (maximum time of inactivity between two data packets)
                .setSocketTimeout(responseTimeout)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES);

        httpBuilder = HttpClients.custom()
                .setDefaultRequestConfig(config.build())
                .setUserAgent(USER_AGENT);

        // trust all hosts & accept self-signed certificates
        SSLConnectionSocketFactory sslConnectionFactory = sslConnectionFactory();
        if (sslConnectionFactory != null) {
            httpBuilder.setSSLSocketFactory(sslConnectionFactory);
        }

        client = httpBuilder.build();
    }

    private SSLConnectionSocketFactory sslConnectionFactory() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true).build();

            // ignore self-signed certificates
            // https://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient
            return new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"},
                    null,
                    NoopHostnameVerifier.INSTANCE
            );
        } catch (Exception e) {
            return null;
        }
    }

    public void setUserAgent(String userAgent) {
        this.httpBuilder.setUserAgent(userAgent);
        this.client = httpBuilder.build();
    }

    public void setProxy(String proxyUrl, AuthPrincipal principal) {
        HttpHost proxy = HttpHost.create(proxyUrl);
        if (principal != null) {
            credentialsProvider.setCredentials(
                    new AuthScope(proxy),
                    new UsernamePasswordCredentials(principal.getUsername(), principal.getPassword()));
        }
        httpBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
        client = httpBuilder.build();
    }

    public void setBasicAuth(String username, String password, String url) {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        this.httpBuilder.setDefaultCredentialsProvider(credentialsProvider);
        this.client = this.httpBuilder.build();

        // enable preemptive auth (putting auth headers into each request to omit 401 challenge),
        // needs valid URL (submitted by user) to enable
        try {
            if (isNotBlank(url)) {
                URI uri = URI.create(url);
                AuthCache authCache = new BasicAuthCache();
                localContext.setAuthCache(authCache);
                authCache.put(HttpHost.create(uri.getHost()), new BasicScheme());
            }
        } catch (Exception ignored) {}
    }

    public Response execute(Request request) {
        try {
            HttpRequestBase httpRequest = request.createHttpRequest();
            Response response = client.execute(httpRequest, handler, localContext);

            // extract runtime headers from context
            request.getHeaders().putAll(mapHeaders(localContext.getRequest().getAllHeaders()));
            return response;
        } catch (IOException e) {
            return new Response(-1, e.getMessage(), new HashMap<>(), getStackTrace(getRootCause(e)));
        }
    }

    private static Header[] mapHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(e -> new BasicHeader(e.getKey(), e.getValue()))
                .toArray(Header[]::new);
    }

    private static Map<String, String> mapHeaders(Header[] headers) {
        return Arrays.stream(headers)
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

    private static String consumeBodyQuietly(HttpEntity entity) {
        try {
            return entity != null ? EntityUtils.toString(entity) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return "<unable to obtain response body>";
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Request {

        private final String method;
        private final URI uri;
        private final Map<String, String> headers;
        private final String body;

        public Request(String method, String uri, Map<String, String> headers, String body) {
            this.method = method;
            this.uri = URI.create(uri);
            this.headers = new LinkedHashMap<>(headers);
            this.body = body;
        }

        public String getMethod() {
            return method;
        }

        public URI getUri() {
            return uri;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }

        public HttpRequestBase createHttpRequest() {
            HttpRequestBase request = switch (method) {
                case HttpDelete.METHOD_NAME -> new HttpDelete();
                case HttpGet.METHOD_NAME -> new HttpGet();
                case HttpHead.METHOD_NAME -> new HttpHead();
                case HttpPatch.METHOD_NAME -> new HttpPatch();
                case HttpPost.METHOD_NAME -> new HttpPost();
                case HttpPut.METHOD_NAME -> new HttpPut();
                default -> throw new IllegalArgumentException("Unsupported HTTP method name");
            };

            request.setURI(uri);
            request.setHeaders(mapHeaders(headers));

            if (request instanceof HttpEntityEnclosingRequestBase) {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new ByteArrayEntity(body.getBytes()));
            }

            return request;
        }
    }

    public static class Response {

        private final int status;
        private final String reasonPhrase;
        private final Map<String, String> headers;
        private final String body;

        public Response(int status, String reasonPhrase, Map<String, String> headers, String body) {
            this.status = status;
            this.reasonPhrase = reasonPhrase;
            this.headers = new LinkedHashMap<>(headers);
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }
    }

    // HTTP DELETE method with body support
    private static class HttpDelete extends HttpEntityEnclosingRequestBase {

        public static final String METHOD_NAME = "DELETE";

        public HttpDelete() {
            super();
        }

        public HttpDelete(String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDelete(URI uri) {
            super();
            setURI(uri);
        }

        public String getMethod() {
            return METHOD_NAME;
        }
    }

    public static class SpecificResponseHandler implements ResponseHandler<Response> {

        public Response handleResponse(HttpResponse response) {
            StatusLine statusLine = response.getStatusLine();
            return new Response(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase(),
                    mapHeaders(response.getAllHeaders()),
                    consumeBodyQuietly(response.getEntity())
            );
        }
    }
}
