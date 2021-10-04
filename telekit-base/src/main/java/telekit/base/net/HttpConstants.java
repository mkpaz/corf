package telekit.base.net;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.UnaryOperator.identity;

public class HttpConstants {

    public static final Set<String> SSL_PROTOCOLS = Set.of(
            "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"
    );

    public enum Method {DELETE, GET, HEAD, PATCH, POST, PUT}

    public enum AuthScheme {BASIC, DIGEST, BEARER}

    public static class Headers {

        public static final String ACCEPT = "Accept";
        public static final String ACCEPT_CHARSET = "Accept-Charset";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String ACCEPT_RANGES = "Accept-Ranges";
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
        public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
        public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
        public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
        public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
        public static final String AGE = "Age";
        public static final String ALLOW = "Allow";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String CONNECTION = "Connection";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String CONTENT_DISPOSITION = "Content-Disposition";
        public static final String CONTENT_LANGUAGE = "Content-Language";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String CONTENT_LOCATION = "Content-Location";
        public static final String CONTENT_RANGE = "Content-Range";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String COOKIE = "Cookie";
        public static final String DATE = "Date";
        public static final String ETAG = "ETag";
        public static final String EXPECT = "Expect";
        public static final String EXPIRES = "Expires";
        public static final String FROM = "From";
        public static final String HOST = "Host";
        public static final String IF_MATCH = "If-Match";
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        public static final String IF_NONE_MATCH = "If-None-Match";
        public static final String IF_RANGE = "If-Range";
        public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
        public static final String LAST_MODIFIED = "Last-Modified";
        public static final String LINK = "Link";
        public static final String LOCATION = "Location";
        public static final String MAX_FORWARDS = "Max-Forwards";
        public static final String ORIGIN = "Origin";
        public static final String PRAGMA = "Pragma";
        public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
        public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
        public static final String RANGE = "Range";
        public static final String REFERER = "Referer";
        public static final String RETRY_AFTER = "Retry-After";
        public static final String SERVER = "Server";
        public static final String SET_COOKIE = "Set-Cookie";
        public static final String SET_COOKIE2 = "Set-Cookie2";
        public static final String TE = "TE";
        public static final String TRAILER = "Trailer";
        public static final String TRANSFER_ENCODING = "Transfer-Encoding";
        public static final String UPGRADE = "Upgrade";
        public static final String USER_AGENT = "User-Agent";
        public static final String VARY = "Vary";
        public static final String VIA = "Via";
        public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    }

    public enum ContentType {

        APPLICATION_JSON("application/json"),
        APPLICATION_OCTET_STREAM("application/octet-stream"),
        APPLICATION_SOAP_XML("application/soap+xml"),
        APPLICATION_XML("application/xml"),
        TEXT_PLAIN("text/plain"),
        TEXT_XML("text/xml");

        private static final Map<String, ContentType> MIME_INDEX = Stream.of(ContentType.values())
                .collect(Collectors.toMap(ContentType::getMimeType, identity()));

        private final String mimeType;

        ContentType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return mimeType;
        }

        @JsonCreator
        public static ContentType fromValue(String mimeType) {
            return MIME_INDEX.get(mimeType);
        }

        @JsonValue
        public String toValue() {
            return this.mimeType;
        }

        public String toHeader(@Nullable Charset charset) {
            String start = "Content-Type: " + mimeType;
            return charset != null ? start + "; charset=" + charset.displayName().toLowerCase() : start;
        }
    }
}
