package corf.desktop.tools.base64;

import java.util.Base64;

import static java.util.Base64.*;

public enum Codec {

    BASIC("Basic"),
    URL_SAFE("URL"),
    MIME("MIME");

    private final String displayName;

    Codec(String displayName) {
        this.displayName = displayName;
    }

    public Encoder getEncoder() {
        return switch (this) {
            case URL_SAFE -> getUrlEncoder();
            case MIME -> getMimeEncoder();
            default -> Base64.getEncoder();
        };
    }

    public Decoder getDecoder() {
        return switch (this) {
            case URL_SAFE -> getUrlDecoder();
            case MIME -> getMimeDecoder();
            default -> Base64.getDecoder();
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
