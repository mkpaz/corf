package corf.desktop.tools.filebuilder;

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
                styledText("INSERT INTO table (index, fruit, color, count) VALUES (", MONOSPACE),
                styledText("${_index1}", ACCENT, MONOSPACE),
                styledText(",  "),
                styledText("'${_csv0}'", SUCCESS, MONOSPACE),
                styledText(",  "),
                styledText("'${color}'", DANGER, MONOSPACE),
                styledText(",  "),
                styledText("${_csv1}", SUCCESS, MONOSPACE),
                styledText(");", MONOSPACE)
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
                styledText("INSERT INTO table (index, fruit, color, count) VALUES (1, 'apple', 'lime', 8);\n", MONOSPACE),
                styledText("INSERT INTO table (index, fruit, color, count) VALUES (2, 'mango', 'fuchsia', 3);\n", MONOSPACE),
                styledText("INSERT INTO table (index, fruit, color, count) VALUES (3, 'lemon', 'magenta', 5);", MONOSPACE)
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
