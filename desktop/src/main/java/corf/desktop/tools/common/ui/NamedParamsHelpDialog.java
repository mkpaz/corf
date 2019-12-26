package corf.desktop.tools.common.ui;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import corf.base.desktop.controls.ModalDialog;
import corf.base.event.BrowseEvent;
import corf.base.event.Events;
import corf.desktop.i18n.DM;
import corf.desktop.tools.common.Param;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Arrays;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;
import static corf.base.desktop.ExtraStyles.MONOSPACE;

public class NamedParamsHelpDialog extends ModalDialog {

    public NamedParamsHelpDialog() {
        super();
        setContent(createContent());
    }

    private Content createContent() {
        var body = new GridPane();
        body.setHgap(40);
        body.setVgap(20);

        body.add(styledText(t(DM.TYPE), TEXT_BOLD, TEXT_SUBTLE), 0, 0);
        body.add(styledText(t(DM.OPTIONS), TEXT_BOLD, TEXT_SUBTLE), 1, 0);
        body.add(styledText(t(DM.EXAMPLE), TEXT_BOLD, TEXT_SUBTLE), 2, 0);
        body.add(styledText(t(DM.RESULT), TEXT_BOLD, TEXT_SUBTLE), 3, 0);

        body.add(styledText(Param.Type.CONSTANT.name(), MONOSPACE), 0, 1);
        body.add(styledText("n/a", MONOSPACE), 1, 1);
        body.add(styledText("", MONOSPACE), 2, 1);
        body.add(createTextField(), 3, 1);

        body.add(styledText(Param.Type.CHOICE.name(), MONOSPACE), 0, 2);
        body.add(styledText("array", MONOSPACE), 1, 2);
        body.add(styledText("1,2,3", MONOSPACE), 2, 2);
        body.add(createComboBox("1", "2", "3"), 3, 2);

        var datafakerParam = new Param("datafaker", Param.Type.DATAFAKER, "#{color.name}", null);
        body.add(createDatafakerLink(), 0, 3);
        body.add(styledText("expression", MONOSPACE), 1, 3);
        body.add(styledText(datafakerParam.getOption(), MONOSPACE), 2, 3);
        body.add(styledText(datafakerParam.resolve().getValue(), MONOSPACE), 3, 3);

        var passwordParam = new Param("password", Param.Type.PASSWORD, "12", null);
        body.add(styledText(passwordParam.getType().name(), MONOSPACE), 0, 4);
        body.add(styledText("length", MONOSPACE), 1, 4);
        body.add(styledText(passwordParam.getOption(), MONOSPACE), 2, 4);
        body.add(styledText(passwordParam.resolve().getValue(), MONOSPACE), 3, 4);

        var password64Param = new Param("password64", Param.Type.PASSWORD_BASE64, "12", null);
        body.add(styledText(password64Param.getType().name(), MONOSPACE), 0, 5);
        body.add(styledText("length", MONOSPACE), 1, 5);
        body.add(styledText(password64Param.getOption(), MONOSPACE), 2, 5);
        body.add(styledText(password64Param.resolve().getValue(), MONOSPACE), 3, 5);

        var timestampParam = new Param("timestamp", Param.Type.TIMESTAMP, "n/a", null);
        body.add(styledText(timestampParam.getType().name(), MONOSPACE), 0, 6);
        body.add(styledText("n/a", MONOSPACE), 1, 6);
        body.add(styledText("", MONOSPACE), 2, 6);
        body.add(styledText(timestampParam.resolve().getValue(), MONOSPACE), 3, 6);

        var uuidParam = new Param("uuid", Param.Type.UUID, "n/a", null);
        body.add(styledText(uuidParam.getType().name(), MONOSPACE), 0, 7);
        body.add(styledText("n/a", MONOSPACE), 1, 7);
        body.add(styledText("", MONOSPACE), 2, 7);
        body.add(styledText(uuidParam.resolve().getValue(), MONOSPACE), 3, 7);

        return Content.create(t(DM.HELP), body, null);
    }

    private Text styledText(@Nullable String s, String... styles) {
        var text = new Text(s);
        text.getStyleClass().addAll(TEXT);
        text.getStyleClass().addAll(styles);
        return text;
    }

    private Hyperlink createDatafakerLink() {
        var link = new Hyperlink(Param.Type.DATAFAKER.name());
        link.setOnAction(e -> {
            var uri = URI.create("https://www.datafaker.net/documentation/expressions/");
            Events.fire(new BrowseEvent(uri));
        });
        return link;
    }

    private TextField createTextField() {
        var tf = new TextField();
        tf.setPromptText("any value");
        tf.setMinWidth(200);
        tf.setMaxWidth(200);
        return tf;
    }

    private ComboBox<String> createComboBox(String... items) {
        var cmb = new ComboBox<>(FXCollections.observableList(Arrays.asList(items)));
        cmb.getSelectionModel().selectFirst();
        cmb.setMinWidth(200);
        cmb.setMaxWidth(200);
        return cmb;
    }
}
