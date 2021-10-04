package telekit.desktop.tools.common;

import javafx.scene.control.TextArea;
import telekit.base.domain.LineSeparator;

import static org.apache.commons.lang3.StringUtils.*;
import static telekit.base.util.CSVUtils.COMMA_OR_SEMICOLON;
import static telekit.base.util.CSVUtils.addColumnsTheRight;
import static telekit.base.util.DesktopUtils.getFromClipboard;

public final class ComponentUtils {

    public static void pasteFromExcel(TextArea textArea) {
        String clipboardText = getFromClipboard();
        if (isBlank(clipboardText)) { return; }

        String currentText = textArea.getText();
        String newText = trim(clipboardText.replaceAll("\t", ","));

        if (isEmpty(currentText)) {
            textArea.setText(newText);
        } else {
            textArea.replaceText(0, currentText.length(), newText);
        }
    }

    public static void pasteAsColumns(TextArea textArea) {
        String clipboardText = trim(getFromClipboard());
        if (isBlank(clipboardText)) { return; }

        String currentText = textArea.getText();

        if (isEmpty(currentText)) {
            textArea.setText(clipboardText);
        } else {
            int origLen = textArea.getText().length();
            String newText = addColumnsTheRight(currentText, clipboardText, COMMA_OR_SEMICOLON);
            textArea.replaceText(0, origLen, newText);
        }
    }

    public static int countNotBlankLines(String text) {
        int count = 0;
        if (isNotBlank(text)) {
            String textWithoutEmptyLines = text.replaceAll("(?m)^[ \t]*\r?\n", "");
            count = textWithoutEmptyLines.split(LineSeparator.LINE_SPLIT_PATTERN, -1).length;
        }
        return count;
    }
}
