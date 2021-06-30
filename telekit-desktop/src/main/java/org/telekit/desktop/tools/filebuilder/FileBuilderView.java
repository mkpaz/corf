package org.telekit.desktop.tools.filebuilder;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.di.Initializable;
import org.telekit.base.util.TextBuilder;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.BindUtils;
import org.telekit.controls.util.Controls;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;

import static javafx.scene.layout.GridPane.REMAINING;
import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.i18n.ControlsMessages.*;
import static org.telekit.controls.util.BindUtils.isBlank;
import static org.telekit.controls.util.Containers.gridPane;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.desktop.i18n.DesktopMessages.*;
import static org.telekit.desktop.tools.filebuilder.FileBuilderViewModel.*;

@Singleton
public final class FileBuilderView extends VBox implements Initializable, View<FileBuilderViewModel> {

    SettingsPane settingsPane;

    ToggleGroup saveTypeToggle;
    TextField destPathText;
    CheckBox appendToFileCheck;
    CheckBox openAfterGenerationCheck;
    Button generateBtn;

    private final FileBuilderViewModel model;
    private final Overlay overlay;

    @Inject
    public FileBuilderView(FileBuilderViewModel model, Overlay overlay) {
        this.model = model;
        this.overlay = overlay;

        createView();
    }

    private void createView() {
        settingsPane = new SettingsPane(this);
        VBox.setVgrow(settingsPane, Priority.ALWAYS);

        // GRID

        saveTypeToggle = new ToggleGroup();
        model.saveTypeProperty().bind(saveTypeToggle.selectedToggleProperty());

        RadioButton dynamicPathCheck = new RadioButton(t(TOOLS_SHOW_SAVE_DIALOG));
        dynamicPathCheck.setUserData(SAVE_TYPE_DYNAMIC);
        dynamicPathCheck.setToggleGroup(saveTypeToggle);
        dynamicPathCheck.setSelected(true);

        RadioButton predefinedPathCheck = new RadioButton(t(TOOLS_SAVE_AS));
        predefinedPathCheck.setUserData(SAVE_TYPE_PREDEFINED);
        predefinedPathCheck.setToggleGroup(saveTypeToggle);

        destPathText = new TextField(DEFAULT_PATH.toString());
        destPathText.setPrefWidth(400);

        Button browseBtn = Controls.create(() -> new Button(t(ACTION_BROWSE)), "link-button");
        browseBtn.setCursor(Cursor.HAND);
        browseBtn.setOnAction(e -> selectDestPath());

        appendToFileCheck = Controls.create(() -> new CheckBox(t(TOOLS_APPEND_IF_EXISTS)), "text-sm");
        appendToFileCheck.selectedProperty().bindBidirectional(model.appendToFileProperty());

        openAfterGenerationCheck = Controls.create(() -> new CheckBox(t(FILEBUILDER_OPEN_FILE_AFTER_GENERATION)), "text-sm");
        openAfterGenerationCheck.selectedProperty().bindBidirectional(model.openAfterGenerationProperty());

        generateBtn = button(t(ACTION_GENERATE), Material2MZ.SHUFFLE, "large");
        generateBtn.setOnAction(e -> generate());

        GridPane controlsGrid = gridPane(5, 5, new Insets(10));

        controlsGrid.add(dynamicPathCheck, 0, 0);

        controlsGrid.add(predefinedPathCheck, 0, 1);
        controlsGrid.add(destPathText, 1, 1);
        controlsGrid.add(browseBtn, 2, 1);

        controlsGrid.add(appendToFileCheck, 1, 2, REMAINING, 1);

        controlsGrid.add(generateBtn, 0, 3, REMAINING, 1);
        controlsGrid.add(openAfterGenerationCheck, 0, 4, REMAINING, 1);

        getChildren().setAll(settingsPane, controlsGrid);
        setId("file-builder");
    }

    @Override
    public void initialize() {
        generateBtn.disableProperty().bind(BindUtils.or(
                model.selectedTemplateProperty().isNull(),
                model.ongoingProperty(),
                isBlank(model.csvTextProperty())
        ));

        // refresh params table
        model.selectFirstTemplate();
    }

    private void selectDestPath() {
        File destFile = Dialogs.fileChooser()
                .addFilter(t(FILE_DIALOG_TEXT), "*.txt")
                .build()
                .showSaveDialog(getWindow());
        if (destFile == null) { return; }

        destPathText.setText(destFile.getAbsolutePath());
    }

    private void generate() {
        List<String> warnings = model.validate();
        if (!warnings.isEmpty()) {
            TextBuilder text = new TextBuilder();
            text.appendLine(t(TOOLS_MSG_VALIDATION_HEAD));
            text.newLine();
            text.appendLines(warnings);
            text.newLine();
            text.append(t(TOOLS_MSG_VALIDATION_TAIL));

            Label label = new Label(text.toString());
            label.setWrapText(true);

            Alert dialog = Dialogs.confirm()
                    .title(t(WARNING))
                    .owner(getWindow())
                    .content("")
                    .build();
            dialog.getDialogPane().setContent(label);

            if (dialog.showAndWait().filter(type -> type == ButtonType.OK).isEmpty()) { return; }
        }

        // determine destination file
        int saveType = (int) model.saveTypeProperty().get().getUserData();
        File outputFile = SAVE_TYPE_PREDEFINED == saveType ?
                new File(destPathText.getText()) :
                Dialogs.fileChooser()
                        .initialFileName(DEFAULT_PATH.getFileName().toString())
                        .build()
                        .showSaveDialog(getWindow());
        if (outputFile == null) { return; }

        model.generateCommand().execute(outputFile);
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public FileBuilderViewModel getViewModel() { return model; }

    ///////////////////////////////////////////////////////////////////////////

    void showOverlay(Pane content) {
        overlay.show(content);
    }

    void hideOverlay() {
        overlay.hide();
    }

    static MenuItem createMenuItem(String text, EventHandler<ActionEvent> handler, BooleanBinding disableCondition) {
        MenuItem item = Controls.menuItem(text, null, handler);
        if (disableCondition != null) { item.disableProperty().bind(disableCondition); }
        return item;
    }
}
