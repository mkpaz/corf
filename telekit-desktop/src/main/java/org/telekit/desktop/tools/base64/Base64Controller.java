package org.telekit.desktop.tools.base64;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.domain.LineSeparator;
import org.telekit.base.domain.event.Notification;
import org.telekit.base.event.DefaultEventBus;

import java.util.Arrays;
import java.util.Base64;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

@FxmlPath("/org/telekit/desktop/tools/base64/_root.fxml")
public class Base64Controller implements Component {

    private static final byte MODE_BY_LINE = 0;
    private static final byte MODE_BY_TEXT = 1;
    private static final byte OPERATION_ENCODE = 10;
    private static final byte OPERATION_DECODE = 11;

    public @FXML TextArea taText;
    public @FXML TextArea taBase64;
    public @FXML ToggleGroup toggleMode;
    public @FXML RadioButton rbModeByLine;
    public @FXML RadioButton rbModeAsText;
    public @FXML ComboBox<EncoderType> encoderType;
    public GridPane rootPane;

    @FXML
    public void initialize() {
        rbModeByLine.setUserData(MODE_BY_LINE);
        rbModeAsText.setUserData(MODE_BY_TEXT);
        encoderType.setItems(FXCollections.observableArrayList(EncoderType.values()));
        encoderType.getSelectionModel().select(EncoderType.BASIC);
    }

    @FXML
    public void encode() {
        byte mode = (byte) toggleMode.getSelectedToggle().getUserData();
        String text = trim(taText.getText());
        EncoderType type = encoderType.getSelectionModel().getSelectedItem();
        if (isEmpty(text) || type == null) { return; }

        final ConvertTask task = createTask(text, OPERATION_ENCODE, mode, type);
        task.setOnSucceeded(event -> Platform.runLater(() -> taBase64.setText(task.getValue())));
        new Thread(task).start();
    }

    @FXML
    public void decode() {
        byte mode = (byte) toggleMode.getSelectedToggle().getUserData();
        String base64Text = trim(taBase64.getText());
        EncoderType type = encoderType.getSelectionModel().getSelectedItem();
        if (isEmpty(base64Text) || type == null) { return; }

        final ConvertTask task = createTask(base64Text, OPERATION_DECODE, mode, type);
        task.setOnSucceeded(event -> Platform.runLater(() -> taText.setText(task.getValue())));
        new Thread(task).start();
    }

    @Override
    public Region getRoot() {
        return rootPane;
    }

    @Override
    public void reset() {}

    private ConvertTask createTask(String sourceText, byte operation, byte mode, EncoderType encoderType) {
        ConvertTask task = new ConvertTask(sourceText, operation, mode, encoderType);
        task.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            if (exception != null) {
                DefaultEventBus.getInstance().publish(Notification.error(exception));
            }
        });
        return task;
    }

    ///////////////////////////////////////////////////////////////////////////

    public enum EncoderType {
        BASIC("Basic"), URL_SAFE("URL"), MIME("MIME");

        private final String name;

        EncoderType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Base64.Encoder getEncoder() {
            switch (this) {
                case URL_SAFE -> { return Base64.getUrlEncoder(); }
                case MIME -> { return Base64.getMimeEncoder(); }
            }
            return Base64.getEncoder();
        }

        public Base64.Decoder getDecoder() {
            switch (this) {
                case URL_SAFE -> { return Base64.getUrlDecoder(); }
                case MIME -> { return Base64.getMimeDecoder(); }
            }
            return Base64.getDecoder();
        }

        // uses to display value name in UI
        @Override
        public String toString() {
            return name;
        }
    }

    // source/target encoding is UTF-8 only
    public static class ConvertTask extends Task<String> {

        private final String sourceText;
        private final byte mode;
        private Function<String, String> converter;

        public ConvertTask(String sourceText, byte operation, byte mode, EncoderType encoderType) {
            this.sourceText = sourceText;
            this.mode = mode;

            this.converter = null;
            if (operation == OPERATION_ENCODE) {
                final Base64.Encoder encoder = encoderType.getEncoder();
                converter = s -> new String(encoder.encode(s.getBytes()));
            }
            if (operation == OPERATION_DECODE) {
                final Base64.Decoder decoder = encoderType.getDecoder();
                converter = s -> new String(decoder.decode(s.getBytes()));
            }
        }

        @Override
        public String call() {
            if (mode == MODE_BY_LINE) {
                Stream<String> stream = Arrays.stream(sourceText.split(LineSeparator.LINE_SPLIT_PATTERN))
                        .filter(e -> !e.isBlank())
                        .map(String::trim);
                return convert(stream, LineSeparator.UNIX.getCharacters(), converter);
            } else {
                return converter.apply(sourceText);
            }
        }

        private static String convert(Stream<String> source,
                                      String lineSeparator,
                                      Function<String, String> converter) {
            return source.map(converter).collect(Collectors.joining(lineSeparator));
        }
    }
}
