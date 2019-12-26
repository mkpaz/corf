package corf.desktop.tools.filebuilder;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import corf.base.desktop.Dialogs;
import corf.base.desktop.controls.Message;
import corf.base.desktop.controls.ModalDialog;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.tools.common.ReplacementCheckResult;
import corf.desktop.tools.common.SaveMode;

import java.io.File;
import java.util.stream.Collectors;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

final class SaveDialog extends ModalDialog {

    private static final int DIALOG_WIDTH = 600;

    VBox messageBox;

    RadioButton newFileRadio;
    Label newFileLabel;
    Button newFileBtn;
    Label newFileNameLabel;

    RadioButton appendFileRadio;
    Label appendFileLabel;
    Button appendFileBtn;
    Label appendFileNameLabel;

    RadioButton clipboardRadio;
    Label clipboardLabel;

    Button applyBtn;
    Button closeBtn;

    private final FileBuilderViewModel model;

    @SuppressWarnings("NullAway.Init")
    public SaveDialog(FileBuilderViewModel model) {
        super();

        this.model = model;

        setContent(createContent());
        init();
    }

    private Content createContent() {
        messageBox = new VBox();
        messageBox.setPadding(new Insets(0, 0, Recommends.CONTENT_SPACING, 0));

        // ~

        clipboardRadio = new RadioButton();
        clipboardRadio.setToggleGroup(model.getSaveModeGroup());
        clipboardRadio.setUserData(SaveMode.CLIPBOARD);
        clipboardRadio.setSelected(true);

        clipboardLabel = new Label(t(DM.COPY_TO_CLIPBOARD));

        // ~

        newFileRadio = new RadioButton();
        newFileRadio.setToggleGroup(model.getSaveModeGroup());
        newFileRadio.setUserData(SaveMode.NEW_FILE);

        newFileLabel = new Label(t(DM.FILE_BUILDER_CREATE_NEW_FILE));

        newFileBtn = new Button("", new FontIcon(Material2OutlinedAL.FOLDER));
        newFileBtn.getStyleClass().addAll(FLAT);
        newFileBtn.setCursor(Cursor.HAND);
        newFileBtn.setPadding(new Insets(3));

        newFileNameLabel = new Label();
        newFileNameLabel.setGraphic(newFileBtn);
        newFileNameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        newFileNameLabel.setMaxWidth(DIALOG_WIDTH - 100);
        newFileNameLabel.getStyleClass().add(TEXT_SUBTLE);

        // ~

        appendFileRadio = new RadioButton();
        appendFileRadio.setToggleGroup(model.getSaveModeGroup());
        appendFileRadio.setUserData(SaveMode.APPEND_FILE);

        appendFileLabel = new Label(t(DM.FILE_BUILDER_APPEND_TO_THE_PREVIOUS_FILE));

        appendFileBtn = new Button("", new FontIcon(Material2OutlinedAL.FOLDER));
        appendFileBtn.getStyleClass().addAll(FLAT);
        appendFileBtn.setCursor(Cursor.HAND);
        appendFileBtn.setPadding(new Insets(3));

        appendFileNameLabel = new Label();
        appendFileNameLabel.setGraphic(appendFileBtn);
        appendFileNameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        appendFileNameLabel.setMaxWidth(DIALOG_WIDTH - 100);
        appendFileNameLabel.getStyleClass().add(TEXT_SUBTLE);

        var choiceGrid = new GridPane();
        choiceGrid.setHgap(10);
        choiceGrid.setVgap(5);
        choiceGrid.add(clipboardRadio, 0, 0);
        choiceGrid.add(clipboardLabel, 1, 0);
        choiceGrid.add(new Spacer(Recommends.CONTENT_SPACING - choiceGrid.getVgap(), Orientation.VERTICAL), 0, 1);
        choiceGrid.add(newFileRadio, 0, 2);
        choiceGrid.add(newFileLabel, 1, 2);
        choiceGrid.add(newFileNameLabel, 1, 3);
        choiceGrid.add(new Spacer(Recommends.CONTENT_SPACING - choiceGrid.getVgap(), Orientation.VERTICAL), 0, 4);
        choiceGrid.add(appendFileRadio, 0, 5);
        choiceGrid.add(appendFileLabel, 1, 5);
        choiceGrid.add(appendFileNameLabel, 1, 6);

        var body = new VBox();
        body.getChildren().addAll(messageBox, choiceGrid);
        body.setPrefWidth(DIALOG_WIDTH);

        // == FOOTER ==

        applyBtn = new Button(t(DM.ACTION_SAVE));
        applyBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var footer = new HBox(Recommends.FORM_INLINE_SPACING, new Spacer(), applyBtn, closeBtn);

        return Content.create(t(DM.ACTION_SAVE), body, footer);
    }

    private void init() {
        newFileNameLabel.setText(String.valueOf(model.getNewFile()));
        model.newFileProperty().addListener(
                (obs, old, val) -> newFileNameLabel.setText(String.valueOf(model.getNewFile()))
        );

        newFileBtn.setOnAction(e -> {
            File file = Dialogs.fileChooser()
                    .initialDirectory(model.getNewFileDir())
                    .initialFileName(String.valueOf(model.getNewFile().getFileName()))
                    .build()
                    .showSaveDialog(getScene().getWindow());
            if (file == null) { return; }

            model.newFileProperty().set(file.toPath());
        });

        appendFileNameLabel.setText(String.valueOf(model.getAppendFile()));
        model.appendFileProperty().addListener(
                (obs, old, val) -> appendFileNameLabel.setText(String.valueOf(model.getAppendFile()))
        );

        appendFileBtn.setOnAction(e -> {
            File file = Dialogs.fileChooser()
                    .initialDirectory(model.getAppendFileDir())
                    .initialFileName(String.valueOf(model.getAppendFile().getFileName()))
                    .build()
                    .showOpenDialog(getScene().getWindow());
            if (file == null) { return; }

            model.appendFileProperty().set(file.toPath());
        });

        applyBtn.setOnAction(e -> {
            model.generateCommand().run();
            close();
        });

        closeBtn.setOnAction(e -> close());

        messageBox.managedProperty().bind(Bindings.size(messageBox.getChildren()).greaterThan(0));
    }

    public void prepare() {
        messageBox.getChildren().clear();

        ReplacementCheckResult check = model.validate();
        if (!check.passed()) {
            var errors = check.getWarnings().stream()
                    .map(s -> "• " + s)
                    .collect(Collectors.joining("\n"));
            var message = new Message(Message.Type.WARNING, t(DM.WARNING), errors);
            messageBox.getChildren().setAll(message);

            Styles.addStyleClass(applyBtn, DANGER, SUCCESS);
        } else {
            Styles.addStyleClass(applyBtn, SUCCESS, DANGER);
        }

        clipboardRadio.setSelected(true);

        boolean hasHistory = model.appendFileProperty().get() == null;
        appendFileRadio.setDisable(hasHistory);
        appendFileBtn.setDisable(hasHistory);
        appendFileNameLabel.setDisable(hasHistory);
        appendFileLabel.setDisable(hasHistory);
    }
}
