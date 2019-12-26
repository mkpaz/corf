package org.telekit.desktop.tools.base64;

import java.util.Base64;

import static java.util.Base64.*;

public enum CodecType {

    BASIC("Basic"),
    URL_SAFE("URL"),
    MIME("MIME");

    private final String displayName;

    CodecType(String displayName) {
        this.displayName = displayName;
    }

    public Encoder getEncoder() {
        switch (this) {
            case URL_SAFE -> { return getUrlEncoder(); }
            case MIME -> { return getMimeEncoder(); }
        }
        return Base64.getEncoder();
    }

    public Decoder getDecoder() {
        switch (this) {
            case URL_SAFE -> { return getUrlDecoder(); }
            case MIME -> { return getMimeDecoder(); }
        }
        return Base64.getDecoder();
    }

    @Override
    public String toString() {
        return displayName;
    }
}