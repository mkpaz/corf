package corf.desktop.tools.ipcalc;

import atlantafx.base.controls.Spacer;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.desktop.RegexUnaryOperator;
import corf.base.desktop.controls.HorizontalForm;
import corf.base.desktop.controls.ModalDialog;
import corf.base.net.IPv4Host;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import static atlantafx.base.theme.Styles.STATE_DANGER;
import static corf.base.i18n.I18n.t;

final class ConverterDialog extends ModalDialog {

    private static final int DIALOG_WIDTH = 500;

    TextField octetIPText;
    TextField binaryIPText;
    TextField hexIPText;
    TextField integerIPText;
    Button commitBtn;
    Button closeBtn;

    private Consumer<String> commitHandler;

    @SuppressWarnings("NullAway.Init")
    public ConverterDialog() {
        super();

        setContent(createContent());
        init();
    }

    private Content createContent() {
        octetIPText = new TextField();
        octetIPText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(octetIPText, Priority.ALWAYS);

        binaryIPText = new TextField();
        binaryIPText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(binaryIPText, Priority.ALWAYS);

        hexIPText = new TextField();
        hexIPText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(hexIPText, Priority.ALWAYS);

        integerIPText = new TextField();
        integerIPText.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(integerIPText, Priority.ALWAYS);

        var body = new HorizontalForm();
        body.setHgap(Recommends.FORM_HGAP);
        body.setVgap(Recommends.FORM_VGAP);
        body.setPrefWidth(DIALOG_WIDTH);

        body.add(t(DM.IP_ADDRESS), octetIPText);
        body.add("Binary", binaryIPText);
        body.add("Hex", hexIPText);
        body.add("Integer", integerIPText);

        // == FOOTER ==

        commitBtn = new Button(t(DM.ACTION_APPLY));
        commitBtn.setDefaultButton(true);
        commitBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var footer = new HBox(Recommends.FORM_INLINE_SPACING, new Spacer(), commitBtn, closeBtn);

        return Content.create(t(DM.IPV4CALC_IP_ADDRESS_CONVERTER), body, footer);
    }

    private void init() {
        octetIPText.setTextFormatter(RegexUnaryOperator.createTextFormatter(Pattern.compile(IPv4Host.PATTERN)));
        octetIPText.textProperty().addListener((obs, old, val) -> updateAll(parseInput(val, -1)));

        binaryIPText.textProperty().addListener((obs, old, val) -> updateAll(parseInput(val, 2)));
        hexIPText.textProperty().addListener((obs, old, val) -> updateAll(parseInput(val, 16)));
        integerIPText.textProperty().addListener((obs, old, val) -> updateAll(parseInput(val, 10)));

        commitBtn.setOnAction(e -> commit());
        closeBtn.setOnAction(e -> close());
    }

    public void setHost(String ipStr) {
        if (IPv4Host.isValidString(ipStr)) {
            octetIPText.setText(ipStr);
        } else {
            octetIPText.setText(IPv4CalcViewModel.DEFAULT_IP);
        }
    }

    public void setOnCommit(Consumer<String> handler) {
        this.commitHandler = handler;
    }

    private void commit() {
        if (commitHandler != null) {
            commitHandler.accept(octetIPText.getText());
        }
    }

    private @Nullable IPv4Host parseInput(String ip, int radix) {
        if (StringUtils.isBlank(ip)) { return null; }

        try {
            // parse from octet string
            if (radix == -1) { return new IPv4Host(ip); }

            // parse from number
            long longValue = Long.parseLong(ip, radix);
            if (longValue < IPv4Host.MIN_VALUE || longValue > IPv4Host.MAX_VALUE) {
                return null;
            }

            return new IPv4Host((int) longValue);
        } catch (Throwable e) {
            return null;
        }
    }

    private void updateAll(@Nullable IPv4Host ip) {
        updateOne(octetIPText, ip != null ? ip.toString() : "");
        updateOne(binaryIPText, ip != null ? ip.toBinaryString() : "");
        updateOne(hexIPText, ip != null ? ip.toHexString() : "");
        updateOne(integerIPText, ip != null ? String.valueOf(ip.longValue()) : "");
    }

    private void updateOne(TextField textField, String value) {
        if (textField.isFocused()) {
            // empty means invalid value
            textField.pseudoClassStateChanged(STATE_DANGER, StringUtils.isEmpty(value));
            // don't update focused field text, because user edits it in that very moment
            return;
        }
        Platform.runLater(() -> textField.setText(value));
    }
}
