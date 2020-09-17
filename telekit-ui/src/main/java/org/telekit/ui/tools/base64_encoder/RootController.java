package org.telekit.ui.tools.base64_encoder;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.lang3.StringUtils;
import org.telekit.base.domain.LineSeparator;
import org.telekit.base.fx.Controller;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

public class RootController extends Controller {

    private static final String MODE_BY_LINE = "line";
    private static final String MODE_BY_TEXT = "text";

    public @FXML TextArea taText;
    public @FXML TextArea taBase64;
    public @FXML ToggleGroup toggleMode;
    public @FXML RadioButton rbModeByLine;
    public @FXML RadioButton rbModeAsText;

    @FXML
    public void initialize() {
        rbModeByLine.setUserData(MODE_BY_LINE);
        rbModeAsText.setUserData(MODE_BY_TEXT);
    }

    @FXML
    public void encode() {
        String mode = (String) toggleMode.getSelectedToggle().getUserData();
        String text = trim(taText.getText());
        if (isEmpty(text)) return;

        Base64.Encoder encoder = Base64.getEncoder();
        if (MODE_BY_LINE.equals(mode)) {
            List<String> encoded = Arrays.stream(text.split(LineSeparator.LINE_SPLIT_PATTERN))
                    .filter(StringUtils::isNotBlank)
                    .map(line -> new String(encoder.encode(
                            trim(line).getBytes()))
                    )
                    .collect(Collectors.toList());
            taBase64.setText(String.join("\n", encoded));
        } else {
            taBase64.setText(new String(encoder.encode(text.getBytes())));
        }
    }

    @FXML
    public void decode() {
        String mode = (String) toggleMode.getSelectedToggle().getUserData();
        String base64Text = trim(taBase64.getText());
        if (isEmpty(base64Text)) return;

        Base64.Decoder decoder = Base64.getDecoder();
        if (MODE_BY_LINE.equals(mode)) {
            List<String> decoded = Arrays.stream(base64Text.split(LineSeparator.LINE_SPLIT_PATTERN))
                    .filter(StringUtils::isNotBlank)
                    .map(line -> new String(decoder.decode(
                            trim(line).getBytes()))
                    )
                    .collect(Collectors.toList());
            taText.setText(String.join("\n", decoded));
        } else {
            taText.setText(new String(decoder.decode(base64Text.getBytes())));
        }
    }

    @Override
    public void reset() {}
}
