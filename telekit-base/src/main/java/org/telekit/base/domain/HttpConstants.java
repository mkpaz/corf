package org.telekit.base.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.UnaryOperator.identity;

public class HttpConstants {

    public enum Method {
        DELETE, GET, HEAD, PATCH, POST, PUT
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
    }

    public enum AuthType {
        BASIC, DIGEST
    }
}
