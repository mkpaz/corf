package org.telekit.base.net;

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

    Response execute(Request request);

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

        public Method method() {
            return method;
        }

        public URI uri() {
            return uri;
        }

        public Map<String, String> headers() {
            return headers;
        }

        public String body() {
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

        public String reasonPhrase() {
            return reasonPhrase;
        }

        public Map<String, String> headers() {
            return headers;
        }

        public String body() {
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
