package org.telekit.desktop.tools.common;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.image.Image;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.base.service.CompletionRegistry;

public class ParamIndicatorTableCell extends TableCell<Param, Image> {

    private final CompletionRegistry completionRegistry;

    public ParamIndicatorTableCell(CompletionRegistry completionRegistry) {
        this.completionRegistry = completionRegistry;
    }

    @Override
    public void updateItem(Image image, boolean empty) {
        super.updateItem(image, empty);
        setGraphic(null);

        TableRow<Param> row = getTableRow();
        if (row == null) { return; }

        Param param = row.getItem();
        if (param == null) { return; }

        if (Param.doesSupportCompletion(param, completionRegistry)) {
            setGraphic(new FontIcon(Material2AL.LIGHTBULB));
            return;
        }

        if (param.isAutoGenerated()) {
            setGraphic(new FontIcon(Material2MZ.SHUFFLE));
        }
    }
}