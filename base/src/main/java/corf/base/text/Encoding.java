package corf.base.text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum Encoding {

    ASCII(StandardCharsets.US_ASCII),
    UTF8(StandardCharsets.UTF_8),
    UTF8_BOM(StandardCharsets.UTF_8);

    private final Charset canonicalName;
    public static final Character BOM = '\ufeff';

    Encoding(Charset canonicalName) {
        this.canonicalName = canonicalName;
    }

    public Charset getCharset() {
        return canonicalName;
    }

    public boolean requiresBOM() {
        return this.name().toUpperCase().contains("BOM");
    }
}