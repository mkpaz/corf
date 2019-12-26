package corf.desktop.tools.httpsender;

import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import corf.base.desktop.controls.ModalDialog;
import corf.desktop.i18n.DM;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;
import static corf.base.desktop.ExtraStyles.MONOSPACE;

public class TemplateHelpDialog extends ModalDialog {

    public TemplateHelpDialog() {
        super();
        setContent(createContent());
    }

    private Content createContent() {
        var body = new GridPane();
        body.setHgap(20);
        body.setVgap(10);

        var tpl = new TextFlow();
        tpl.getChildren().setAll(
                styledText("POST: ", TEXT_BOLD, MONOSPACE),
                styledText("https://example.com/fruits\n", MONOSPACE),
                styledText("      { id: ", MONOSPACE),
                styledText("${_index1}", ACCENT, MONOSPACE),
                styledText(", fruit: ", MONOSPACE),
                styledText("\"${_csv0}\"", SUCCESS, MONOSPACE),
                styledText(", color: ", MONOSPACE),
                styledText("\"${color}\"", DANGER, MONOSPACE),
                styledText(", count: ", MONOSPACE),
                styledText("${_csv1}", SUCCESS, MONOSPACE),
                styledText(")\n", MONOSPACE)
        );

        var namedParams = new TextFlow();
        namedParams.getChildren().setAll(
                styledText("color ", TEXT_BOLD, MONOSPACE),
                styledText("[DATAFAKER] = ", MONOSPACE),
                styledText("#{color.name}", DANGER, MONOSPACE)
        );

        var rowParams = new TextFlow();
        rowParams.getChildren().setAll(
                styledText("apple,8\n", SUCCESS, MONOSPACE),
                styledText("mango,3\n", SUCCESS, MONOSPACE),
                styledText("lemon,5", SUCCESS, MONOSPACE)
        );

        var result = new TextFlow();
        result.getChildren().setAll(
                styledText("POST: https://example.com/fruits\n      { id: 1, fruit: \"apple\", color: \"lime\", count: 8);\n", MONOSPACE),
                styledText("POST: https://example.com/fruits\n      { id: 2, fruit: \"mango\", color: \"fuchsia\", count: 3);\n", MONOSPACE),
                styledText("POST: https://example.com/fruits\n      { id: 3, fruit: \"lemon\", color: \"magenta\", count: 5);\n", MONOSPACE)
        );

        body.add(styledText(t(DM.TPL_TEMPLATE), TEXT_BOLD, TEXT_SUBTLE), 0, 0, GridPane.REMAINING, 1);
        body.add(tpl, 0, 1, GridPane.REMAINING, 1);
        body.add(styledText(t(DM.TPL_NAMED_PARAMS), TEXT_BOLD, TEXT_SUBTLE), 0, 2);
        body.add(styledText(t(DM.TPL_ROW_PARAMS), TEXT_BOLD, TEXT_SUBTLE), 1, 2);
        body.add(namedParams, 0, 3);
        body.add(rowParams, 1, 3);
        body.add(styledText(t(DM.RESULT), TEXT_BOLD, TEXT_SUBTLE), 0, 4, GridPane.REMAINING, 1);
        body.add(result, 0, 5, GridPane.REMAINING, 1);

        return Content.create(t(DM.HELP), body, null);
    }

    private Text styledText(String s, String... styles) {
        var text = new Text(s);
        text.getStyleClass().addAll(TEXT);
        text.getStyleClass().addAll(styles);
        return text;
    }
}
