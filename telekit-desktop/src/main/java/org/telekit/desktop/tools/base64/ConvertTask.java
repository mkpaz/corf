package org.telekit.desktop.tools.base64;

import javafx.concurrent.Task;
import org.telekit.base.domain.LineSeparator;

import java.util.Arrays;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.telekit.base.domain.LineSeparator.LINE_SPLIT_PATTERN;

// source/target encoding is UTF-8 only
public class ConvertTask extends Task<String> {

    static final int MODE_BY_LINE = 0;
    static final int MODE_AS_TEXT = 1;
    static final int OPERATION_ENCODE = 10;
    static final int OPERATION_DECODE = 11;

    private final String sourceText;
    private final int mode;
    private Function<String, String> converter;

    public ConvertTask(String sourceText, int operation, int mode, CodecType codec) {

        this.sourceText = sourceText;
        this.mode = mode;

        this.converter = null;
        if (operation == OPERATION_ENCODE) {
            Encoder encoder = codec.getEncoder();
            converter = s -> new String(encoder.encode(s.getBytes(UTF_8)), UTF_8);
        }
        if (operation == OPERATION_DECODE) {
            Decoder decoder = codec.getDecoder();
            converter = s -> new String(decoder.decode(s.getBytes(UTF_8)), UTF_8);
        }
    }

    @Override
    public String call() {
        if (mode == MODE_BY_LINE) {
            return Arrays.stream(sourceText.split(LINE_SPLIT_PATTERN))
                    .filter(line -> !line.isBlank())
                    .map(String::trim)
                    .map(converter)
                    .collect(Collectors.joining(LineSeparator.UNIX.getCharacters()));
        } else {
            return converter.apply(sourceText);
        }
    }
}