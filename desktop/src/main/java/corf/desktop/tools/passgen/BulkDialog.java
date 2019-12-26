package corf.desktop.tools.passgen;

import atlantafx.base.controls.Spacer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.Env;
import corf.base.desktop.Async;
import corf.base.desktop.Dialogs;
import corf.base.desktop.OS;
import corf.base.desktop.controls.ModalDialog;
import corf.base.event.Events;
import corf.base.event.Notification;
import corf.base.exception.AppException;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

final class BulkDialog extends ModalDialog {

    private static final int MIN_COUNT = 100;
    private static final int MAX_COUNT = 10_000;
    private static final int DEFAULT_COUNT = 100;
    private static final int STEP = MIN_COUNT;
    private static final int DIALOG_WIDTH = 600;
    private static final String EXPORT_FILE_NAME = "passwords.txt";

    Label countLabel;
    Slider countSlider;
    Button clipboardBtn;
    Button fileBtn;

    private Generator generator;

    private final ExecutorService executorService;

    @SuppressWarnings("NullAway.Init")
    public BulkDialog(ExecutorService executorService) {
        super();

        this.executorService = executorService;

        setContent(createContent());
        init();
    }

    private Content createContent() {
        var countTitleLabel = new Label(t(DM.COUNT));
        countTitleLabel.getStyleClass().addAll(TEXT_CAPTION);

        countSlider = new Slider(MIN_COUNT, MAX_COUNT, DEFAULT_COUNT);
        countSlider.setBlockIncrement(STEP);

        countLabel = new Label();
        countLabel.getStyleClass().addAll(TEXT_SUBTLE, TEXT_SMALL);

        var countTitleBox = new HBox(countTitleLabel, new Spacer(), countLabel);
        countTitleBox.setAlignment(Pos.CENTER_LEFT);

        var body = new VBox(Recommends.CAPTION_MARGIN, countTitleBox, countSlider);
        body.setPrefWidth(DIALOG_WIDTH);

        // == FOOTER ==

        clipboardBtn = new Button(t(DM.COPY_TO_CLIPBOARD), new FontIcon(Material2OutlinedAL.CONTENT_COPY));
        clipboardBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(clipboardBtn, Priority.ALWAYS);

        fileBtn = new Button(t(DM.SAVE_TO_FILE), new FontIcon(Material2OutlinedMZ.SAVE_ALT));
        fileBtn.setDefaultButton(true);
        fileBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(fileBtn, Priority.ALWAYS);

        var footer = new HBox(Recommends.FORM_INLINE_SPACING, clipboardBtn, fileBtn);
        footer.setAlignment(Pos.CENTER);

        return Content.create(t(DM.ACTION_EXPORT), body, footer);
    }

    public void setGenerator(Generator generator) {
        this.generator = Objects.requireNonNull(generator, "generator");
    }

    private void init() {
        countSlider.valueProperty().addListener(
                (obs, old, val) -> countSlider.setValue((double) Math.round(val.doubleValue() / STEP) * STEP)
        );

        countLabel.textProperty().bind(Bindings.createStringBinding(
                () -> (int) countSlider.getValue() + " " + t(DM.PASSGEN_PASSWORDS).toLowerCase(),
                countSlider.valueProperty()
        ));

        clipboardBtn.setOnAction(e -> exportToClipboard());

        fileBtn.setOnAction(e -> exportToFile());
    }

    private List<String> generate() {
        var count = (int) countSlider.valueProperty().get();
        var passwords = new ArrayList<String>(count);

        for (int i = 0; i < count; i++) {
            passwords.add(generator.generate());
        }

        return passwords;
    }

    private void exportToClipboard() {
        close();

        Async.with(this::generate)
                .setOnSucceeded(passwords -> OS.setClipboard(String.join("\n", passwords)))
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .start(executorService);
    }

    private void exportToFile() {
        File outputFile = Dialogs.fileChooser()
                .addFilter(t(DM.FILE_DIALOG_TEXT), "*.txt")
                .initialDirectory(Env.getLastVisitedDir())
                .initialFileName(EXPORT_FILE_NAME)
                .build()
                .showSaveDialog(getScene().getWindow());
        if (outputFile == null) { return; }

        close();

        Async.with(() -> {
                    List<String> passwords = generate();
                    try {
                        Files.writeString(outputFile.toPath(), String.join("\n", passwords));
                    } catch (Exception e) {
                        throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
                    }
                })
                .setOnSucceeded(nil -> Env.setLastVisitedDir(outputFile))
                .setOnFailed(e -> Events.fire(Notification.error(e)))
                .start(executorService);
    }
}
