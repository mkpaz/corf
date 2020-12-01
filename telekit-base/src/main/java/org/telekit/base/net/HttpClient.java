package org.telekit.base.net;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.net.HttpConstants.Method;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.telekit.base.util.StringUtils.ensureNotNull;

public interface HttpClient {

    String USER_AGENT = "Mozilla/5.0 AppleWebKit/537.36 Chrome/86.0.4240.101 Safari/537.36";
    int CONNECT_TIMEOUT = 5000;
    int RESPONSE_TIMEOUT = 5000;

    @NotNull Response execute(Request request);

    ///////////////////////////////////////////////////////////////////////////

    class Request {

        private final Method method;
        private final URI uri;
        private final Map<String, String> headers;
        private final String body;

        public Request(Method method, URI uri, Map<String, String> headers, String body) {
            this.method = Objects.requireNonNull(method);
            this.uri = Objects.requireNonNull(uri);
            this.headers = headers != null ? new LinkedHashMap<>(headers) : new HashMap<>();
            this.body = ensureNotNull(body);
        }

        public @NotNull Method method() {
            return method;
        }

        public @NotNull URI uri() {
            return uri;
        }

        public @NotNull Map<String, String> headers() {
            return headers;
        }

        public @NotNull String body() {
            return body;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "method=" + method +
                    ", uri=" + uri +
                    ", headers=" + headers +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    class Response {

        private final int statusCode;
        private final String reasonPhrase;
        private final Map<String, String> headers;
        private final String body;

        public Response(int statusCode, String reasonPhrase, Map<String, String> headers, String body) {
            this.statusCode = statusCode;
            this.reasonPhrase = ensureNotNull(reasonPhrase);
            this.headers = headers != null ? new LinkedHashMap<>(headers) : new HashMap<>();
            this.body = ensureNotNull(body);
        }

        public int statusCode() {
            return statusCode;
        }

        public @NotNull String reasonPhrase() {
            return reasonPhrase;
        }

        public @NotNull Map<String, String> headers() {
            return headers;
        }

        public @NotNull String body() {
            return body;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "statusCode=" + statusCode +
                    ", reasonPhrase='" + reasonPhrase + '\'' +
                    ", headers=" + headers +
                    ", body='" + body + '\'' +
                    '}';
        }
    }
}
