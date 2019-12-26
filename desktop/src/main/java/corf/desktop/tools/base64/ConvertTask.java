package corf.desktop.tools.base64;

import javafx.concurrent.Task;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.text.LineSeparator;

import java.util.Arrays;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

// Source / target encoding is UTF-8 only!
final class ConvertTask extends Task<String> {

    private final String sourceText;
    private final Function<String, String> converter;
    private final boolean lineByLine;

    private ConvertTask(String sourceText, Codec codec, boolean encode, boolean lineByLine) {
        this.sourceText = Objects.requireNonNullElse(sourceText, "");
        this.lineByLine = lineByLine;
        this.converter = getConverter(codec, encode);
    }

    @Override
    public String call() {
        return lineByLine
                ? Arrays.stream(sourceText.split(LineSeparator.LINE_SPLIT_PATTERN))
                .filter(line -> !line.isBlank())
                .map(String::trim)
                .map(converter)
                .collect(Collectors.joining(LineSeparator.UNIX.getCharacters()))
                : converter.apply(sourceText);
    }

    public static ConvertTask forEncode(String sourceText, Codec codec, boolean lineByLine) {
        var task = new ConvertTask(sourceText, codec, true, lineByLine);
        task.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            if (exception != null) {
                Events.fire(Notification.error(exception));
            }
        });
        return task;
    }

    public static ConvertTask forDecode(String sourceText, Codec codec, boolean lineByLine) {
        var task = new ConvertTask(sourceText, codec, false, lineByLine);
        task.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            if (exception != null) {
                Events.fire(Notification.error(exception));
            }
        });
        return task;
    }

    private Function<String, String> getConverter(Codec codec, boolean encode) {
        if (encode) {
            Encoder encoder = codec.getEncoder();
            return s -> new String(encoder.encode(s.getBytes(UTF_8)), UTF_8);
        } else {
            Decoder decoder = codec.getDecoder();
            return s -> new String(decoder.decode(s.getBytes(UTF_8)), UTF_8);
        }
    }
}
