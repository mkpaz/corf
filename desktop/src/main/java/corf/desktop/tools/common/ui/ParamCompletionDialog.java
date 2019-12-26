package corf.desktop.tools.common.ui;

import atlantafx.base.controls.Spacer;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import corf.base.common.KeyValue;
import corf.base.desktop.OS;
import corf.base.desktop.controls.FilterTable;
import corf.base.desktop.controls.ModalDialog;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static corf.base.i18n.I18n.t;

public class ParamCompletionDialog extends ModalDialog {

    private static final int PREF_WIDTH = 600;
    private static final int PREF_HEIGHT = 400;

    private FilterTable<KeyValue<String, String>> filterTable;
    private Consumer<KeyValue<String, String>> commitHandler;
    private Path providerPath;

    @SuppressWarnings("NullAway.Init")
    public ParamCompletionDialog() {
        super();

        setContent(createContent());
    }

    private Content createContent() {
        var keyColumn = new TableColumn<KeyValue<String, String>, String>(t(DM.KEY));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

        var valueColumn = new TableColumn<KeyValue<String, String>, String>(t(DM.VALUE));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        filterTable = new FilterTable<>();
        filterTable.setColumns(keyColumn, valueColumn);
        filterTable.setPredicate((filter, row) -> StringUtils.containsIgnoreCase(row.getKey(), filter));
        filterTable.getDataTable().setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                commit();
            }
        });
        VBox.setVgrow(filterTable, Priority.ALWAYS);

        var body = new VBox(filterTable);
        body.setPrefWidth(PREF_WIDTH);
        body.setPrefHeight(PREF_HEIGHT);

        // == FOOTER ==

        var configureLink = new Button(t(DM.ACTION_EDIT), new FontIcon(Material2OutlinedAL.EDIT));
        configureLink.setOnAction(e -> {
            if (providerPath != null) {
                EventQueue.invokeLater(() -> OS.open(providerPath.toFile()));
            }
        });

        var commitBtn = new Button(t(DM.ACTION_OK));
        commitBtn.setDefaultButton(true);
        commitBtn.setOnAction(e -> commit());
        commitBtn.disableProperty().bind(
                filterTable.getDataTable().getSelectionModel().selectedItemProperty().isNull()
        );
        commitBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);
        closeBtn.setOnAction(e -> close());

        var footer = new HBox(Recommends.FORM_INLINE_SPACING);
        footer.getChildren().setAll(configureLink, new Spacer(), commitBtn, closeBtn);

        return Content.create(t(DM.TPL_CHOOSE_VALUE), body, footer);
    }

    public void setData(List<KeyValue<String, String>> data, Path providerPath) {
        filterTable.setData(data);
        filterTable.resetFilter();
        this.providerPath = providerPath;
    }

    public void setCommitHandler(Consumer<KeyValue<String, String>> handler) {
        this.commitHandler = handler;
    }

    private void commit() {
        KeyValue<String, String> kv = filterTable.getSelectedItem();
        if (commitHandler != null && kv != null) {
            commitHandler.accept(kv);
        }
    }
}
