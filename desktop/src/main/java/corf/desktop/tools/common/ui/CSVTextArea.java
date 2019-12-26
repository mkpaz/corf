package corf.desktop.tools.common.ui;

import atlantafx.base.controls.Spacer;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import corf.base.desktop.OS;
import corf.base.text.LineSeparator;
import corf.base.text.CSV;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import static atlantafx.base.theme.Styles.FLAT;
import static corf.base.i18n.I18n.t;

public class CSVTextArea extends VBox {

    private final TextArea textArea = new TextArea();
    private final Label lineCountLabel = new Label();

    public CSVTextArea() {
        super();

        createView();
        init();
    }

    public StringProperty textProperty() {
        return textArea.textProperty();
    }

    private void createView() {
        textArea.setPromptText("csv0, csv1, csv2 ...");
        textArea.getStyleClass().add(ExtraStyles.MONOSPACE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        var pasteMenuBtn = new MenuButton(t(DM.ACTION_PASTE), new FontIcon(Material2OutlinedAL.CONTENT_PASTE));
        pasteMenuBtn.getStyleClass().addAll(FLAT);
        pasteMenuBtn.setPopupSide(Side.RIGHT);
        pasteMenuBtn.getItems().addAll(
                createMenuItem(t(DM.TPL_PASTE_COLUMNS_RIGHT), this::pasteAsColumns),
                createMenuItem(t(DM.TPL_PASTE_FROM_EXCEL), this::pasteFromExcel)
        );

        var actionsBox = new HBox(Recommends.SUB_ITEM_MARGIN);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        actionsBox.getChildren().addAll(
                pasteMenuBtn,
                new Spacer(),
                lineCountLabel,
                new Label(t(DM.TPL_LINES.toLowerCase()))
        );

        setSpacing(Recommends.SUB_ITEM_MARGIN);
        getChildren().setAll(textArea, actionsBox);
    }

    private void init() {
        textArea.focusedProperty().addListener((obs, old, val) -> {
            if (!val) { updateCsvLineCount(); }
        });

        updateCsvLineCount();
    }

    public static MenuItem createMenuItem(String text, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(handler);
        return item;
    }

    private void updateCsvLineCount() {
        // WARNING: avoid binding counting method to the observable property.
        // If text area size is big enough, it will lead to extensive memory
        // usage on multiple subsequent edits.
        var count = countNotBlankLines(StringUtils.trim(textArea.getText()));
        lineCountLabel.setText(String.valueOf(count));
    }

    private int countNotBlankLines(String text) {
        var count = 0;
        if (StringUtils.isNotBlank(text)) {
            var noEmptyLines = text.replaceAll("(?m)^[ \t]*\r?\n", "");
            count = noEmptyLines.split(LineSeparator.LINE_SPLIT_PATTERN, -1).length;
        }
        return count;
    }

    private void pasteFromExcel(ActionEvent e) {
        var clipboardText = StringUtils.trim(OS.getClipboard());
        var currentText = textArea.getText();

        if (StringUtils.isEmpty(clipboardText)) { return; }

        var newText = clipboardText.replaceAll("\t", ",");

        if (StringUtils.isEmpty(currentText)) {
            textArea.setText(newText);
        } else {
            textArea.replaceText(0, currentText.length(), newText);
        }
    }

    private void pasteAsColumns(ActionEvent e) {
        var clipboardText = StringUtils.trim(OS.getClipboard());
        var currentText = textArea.getText();

        if (StringUtils.isEmpty(clipboardText)) { return; }

        if (StringUtils.isEmpty(currentText)) {
            textArea.setText(clipboardText);
        } else {
            var origLen = textArea.getText().length();
            var newText = addColumnsTheRight(currentText, clipboardText);
            textArea.replaceText(0, origLen, newText);
        }
    }

    private String addColumnsTheRight(String origText, String addedText) {
        boolean isOrigBlank = StringUtils.isBlank(origText);
        boolean isAddedBlank = StringUtils.isBlank(addedText);

        if (isOrigBlank && isAddedBlank) { return ""; }
        if (isOrigBlank) { return addedText; }
        if (isAddedBlank) { return origText; }

        CSV origCsv = CSV.from(origText);
        CSV addedCsv = CSV.from(addedText);

        var sb = new StringBuilder();
        for (int rowIndex = 0; rowIndex < origCsv.length(); rowIndex++) {
            String[] curRow = origCsv.get(rowIndex);

            if (rowIndex < addedCsv.length()) {
                String[] newRow = addedCsv.get(rowIndex);
                sb.append(String.join(",", ArrayUtils.addAll(curRow, newRow)));
            } else {
                sb.append(String.join(",", curRow));
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
