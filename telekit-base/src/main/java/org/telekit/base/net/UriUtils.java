package org.telekit.base.net;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.domain.exception.InvalidInputException;

import java.net.URI;
import java.net.URISyntaxException;

public final class UriUtils {

    public static URI parse(String uri) throws InvalidInputException {
        try {
            return URI.create(uri);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid URI: " + uri);
        }
    }

    public static @Nullable URI parseSilently(String uri) {
        try {
            return URI.create(uri);
        } catch (Exception e) {
            return null;
        }
    }

    public static URI withoutPath(URI uri) {
        return withoutPath(uri, false);
    }

    public static URI withoutPath(URI uri, boolean includeUserInfo) {
        try {
            return new URI(
                    uri.getScheme(),
                    includeUserInfo ? uri.getUserInfo() : null,
                    uri.getHost(),
                    uri.getPort(),
                    null,
                    null,
                    null
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
