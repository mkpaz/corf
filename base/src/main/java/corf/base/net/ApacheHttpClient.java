package corf.base.net;

import corf.base.common.NumberUtils;
import corf.base.net.HttpConstants.AuthScheme;
import corf.base.preferences.Proxy;
import corf.base.security.UsernamePasswordCredentials;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.auth.DigestScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;

public class ApacheHttpClient implements HttpClient {

    private static final System.Logger LOGGER = System.getLogger(ApacheHttpClient.class.getName());

    public static final AuthScope AUTH_SCOPE_ANY = new AuthScope(null, null, -1, null, null);
    public static final Set<String> SUPPORTED_SSL_PROTOCOLS = HttpConstants.SSL_PROTOCOLS;

    private final HttpClientResponseHandler<Response> handler = new SpecificResponseHandler();
    private final CloseableHttpClient client;
    private final HttpClientContext localContext;

    private ApacheHttpClient(CloseableHttpClient client, HttpClientContext localContext) {
        this.client = Objects.requireNonNull(client, "client");
        this.localContext = Objects.requireNonNull(localContext, "localContext");
    }

    @Override
    public Response execute(Request request) {
        Objects.requireNonNull(request, "request");
        try {
            HttpUriRequestBase httpRequest = createHttpRequest(request);
            Response response = client.execute(httpRequest, localContext, handler);

            // extract actual request headers from context
            request.headers().putAll(
                    headersToMap(localContext.getRequest().getHeaders())
            );
            return response;
        } catch (IOException e) {
            var err = ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(e));
            return new Response(-1, e.getMessage(), new HashMap<>(), err);
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

    private static @Nullable String consumeBodyQuietly(@Nullable HttpEntity entity) {
        try {
            return entity != null ? EntityUtils.toString(entity) : null;
        } catch (IOException | ParseException e) {
            LOGGER.log(DEBUG, ExceptionUtils.getStackTrace(e));
            return "<unable to obtain response body>";
        }
    }

    private HttpUriRequestBase createHttpRequest(Request request) {
        HttpUriRequestBase requestBase = switch (request.method()) {
            case DELETE -> new HttpDelete(request.uri());
            case GET -> new HttpGet(request.uri());
            case HEAD -> new HttpHead(request.uri());
            case PATCH -> new HttpPatch(request.uri());
            case POST -> new HttpPost(request.uri());
            case PUT -> new HttpPut(request.uri());
        };

        requestBase.setHeaders(mapToHeaders(request.headers()));
        requestBase.setEntity(new ByteArrayEntity(
                request.body().getBytes(StandardCharsets.UTF_8),
                ContentType.TEXT_PLAIN)
        );

        return requestBase;
    }

    public static Builder builder() {
        return new Builder();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Builder {

        private final HttpClientBuilder httpBuilder = HttpClients.custom();
        private final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        private final CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        private final HttpClientContext localContext = HttpClientContext.create();

        public Builder() {
            requestConfigBuilder
                    // Consider switching to another library. Apache HTTP Client architecture and API
                    // is a complete mess of factories provided by builders and consumed by another factories
                    // provided by another builders. It barely documented, so you're spending hours searching
                    // for the builder that provides the factory required by the builder that produces the
                    // factory you presumably have to use to get something to work as expected.
                    .setConnectTimeout(Timeout.of(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS))
                    .setResponseTimeout(Timeout.of(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS));
            httpBuilder.setUserAgent(USER_AGENT);
            httpBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        public Builder timeouts(int timeout) {
            return timeouts(timeout, timeout);
        }

        public Builder timeouts(int connectTimeout, int responseTimeout) {
            requestConfigBuilder.setConnectTimeout(Timeout.of(
                    NumberUtils.ensureRange(connectTimeout, 100, 10_000), TimeUnit.MILLISECONDS)
            );
            requestConfigBuilder.setResponseTimeout(Timeout.of(
                    NumberUtils.ensureRange(responseTimeout, 100, 60_000), TimeUnit.MILLISECONDS)
            );
            return this;
        }

        public Builder userAgent(String userAgent) {
            httpBuilder.setUserAgent(userAgent);
            return this;
        }

        public Builder trustAllCertificates() {
            try {
                var sslContext = SSLContexts.custom()
                        .loadTrustMaterial((chain, authType) -> true)
                        .build();

                var sslSocketFactory = new SSLConnectionSocketFactory(
                        sslContext,
                        SUPPORTED_SSL_PROTOCOLS.toArray(new String[0]),
                        null,
                        NoopHostnameVerifier.INSTANCE
                );

                var connectionManager = new BasicHttpClientConnectionManager(
                        RegistryBuilder.<ConnectionSocketFactory>create()
                                .register(URIScheme.HTTP.id, PlainConnectionSocketFactory.getSocketFactory())
                                .register(URIScheme.HTTPS.id, sslSocketFactory)
                                .build()
                );

                httpBuilder.setConnectionManager(connectionManager);
            } catch (Exception ignored) { /* ignore */ }

            return this;
        }

        public Builder ignoreCookies() {
            requestConfigBuilder.setCookieSpec(StandardCookieSpec.IGNORE);
            return this;
        }

        public Builder proxy(Proxy proxy) {
            Objects.requireNonNull(proxy, "proxy");
            httpBuilder.setRoutePlanner(new CachingProxyRoutePlanner(proxy, credentialsProvider));
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
                                          boolean preemptive) {
            Objects.requireNonNull(authScheme, "authScheme");
            Objects.requireNonNull(auth, "auth");
            Objects.requireNonNull(uri, "uri");

            credentialsProvider.setCredentials(
                    AUTH_SCOPE_ANY,
                    new org.apache.hc.client5.http.auth.UsernamePasswordCredentials(auth.getUserName(), auth.getPassword().clone())
            );

            // preemptive auth puts auth headers into each request to omit challenge (401) stage
            if (!preemptive) { return this; }

            // BasicAuthCache doesn't mean "for basic auth", it's just a misleading name choice
            AuthCache authCache = new BasicAuthCache();
            HttpHost targetHost = new HttpHost(uri.getHost());

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

    ///////////////////////////////////////////////////////////////////////////

    static class SpecificResponseHandler implements HttpClientResponseHandler<Response> {

        @Override
        public Response handleResponse(ClassicHttpResponse response) {
            return new Response(
                    response.getCode(),
                    response.getReasonPhrase(),
                    headersToMap(response.getHeaders()),
                    consumeBodyQuietly(response.getEntity())
            );
        }
    }

    static class CachingProxyRoutePlanner extends DefaultRoutePlanner {

        private final Proxy proxy;
        private final CredentialsStore credentialsStore;

        // routing cache: k = target hostname, v = proxy host or null for direct route
        private final Map<String, HttpHost> routeCache = new HashMap<>();

        // proxy cache to avoid creating multiple equal proxy hosts
        // k = short proxy URI, v = actual proxy host
        private final Map<String, HttpHost> proxyHostCache = new HashMap<>();

        public CachingProxyRoutePlanner(Proxy proxy, CredentialsStore credentialsStore) {
            super(new DefaultSchemePortResolver());

            this.proxy = proxy;
            this.credentialsStore = credentialsStore;
        }

        @Override
        protected @Nullable HttpHost determineProxy(HttpHost target, HttpContext context) {
            LOGGER.log(DEBUG, "Trying to determine proxy params for " + target);
            String targetHostname = target.getHostName();

            // use route from cache, even if null (direct route)
            HttpHost cachedRoute = routeCache.get(targetHostname);
            if (cachedRoute != null) {
                LOGGER.log(DEBUG, "Target host is present in cache, using cached route: " + cachedRoute);
                return cachedRoute;
            }

            ConnectionParams proxyParams = proxy.getConnectionParams(targetHostname);
            if (proxyParams == null) {
                LOGGER.log(DEBUG, "Proxying not required, using direct route");
                routeCache.put(targetHostname, null);
                return null;
            }

            String proxyKey = proxyParams.getScheme().toString() + "://" + proxyParams.getHost() + ":" + proxyParams.getPort();
            HttpHost proxyHost = proxyHostCache.get(proxyKey);

            if (proxyHost == null) {
                proxyHost = new HttpHost(proxyParams.getScheme().toString(), proxyParams.getHost(), proxyParams.getPort());
                LOGGER.log(DEBUG, "Creating new proxy: " + proxyHost);

                // Do not instantiate auth scope from proxyHost, because the latter also
                // includes scheme, There will be no match if proxy uses HTTP and target uses HTTPS.
                AuthScope proxyScope = new AuthScope(proxyParams.getHost(), proxyParams.getPort());

                // credentials are optional for proxy
                UsernamePasswordCredentials cred = null;
                if (proxyParams.getCredentials() instanceof UsernamePasswordCredentials userPassword) {
                    cred = userPassword;
                }

                if (cred != null) {
                    LOGGER.log(DEBUG, "Setting proxy credentials: " + cred);
                    credentialsStore.setCredentials(
                            proxyScope,
                            new org.apache.hc.client5.http.auth.UsernamePasswordCredentials(cred.getUsername(), cred.getPassword())
                    );
                } else {
                    LOGGER.log(DEBUG, "Proxy doesn't require authentication");
                }

                proxyHostCache.put(proxyKey, proxyHost);
            }

            routeCache.put(targetHostname, proxyHost);
            LOGGER.log(DEBUG, "Setting " + proxyHost + " as proxy for the connection");

            return proxyHost;
        }
    }
}
