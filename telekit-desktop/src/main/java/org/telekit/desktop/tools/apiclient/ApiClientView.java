package org.telekit.desktop.tools.apiclient;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.material2.Material2MZ;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.Overlay;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.di.Initializable;
import org.telekit.base.util.TextBuilder;
import org.telekit.controls.dialogs.Dialogs;
import org.telekit.controls.util.BindUtils;
import org.telekit.controls.util.Controls;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static org.telekit.base.i18n.I18n.t;
import static org.telekit.controls.util.BindUtils.isBlank;
import static org.telekit.controls.util.Containers.hbox;
import static org.telekit.controls.util.Containers.stretchedTabPane;
import static org.telekit.controls.util.Controls.button;
import static org.telekit.desktop.i18n.DesktopMessages.*;

@Singleton
public final class ApiClientView extends VBox implements Initializable, View<ApiClientViewModel> {

    TabPane tabPane;
    SettingsTab settingsTab;
    LogTab logTab;

    ProgressBar progressBar;
    Button startBtn;
    Button stopBtn;
    Spinner<Integer> timeoutSpinner;

    private final ApiClientViewModel model;
    private final Overlay overlay;

    @Inject
    public ApiClientView(ApiClientViewModel model, Overlay overlay) {
        this.model = model;
        this.overlay = overlay;

        createView();
    }

    private void createView() {
        // TABS

        settingsTab = new SettingsTab(this);
        logTab = new LogTab(this);

        tabPane = stretchedTabPane(settingsTab, logTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // CONTROLS

        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);

        StackPane progressBox = new StackPane();
        progressBox.setPadding(new Insets(0, 10, 0, 10));
        progressBox.setMinHeight(10);
        progressBox.setMaxHeight(10);
        VBox.setVgrow(progressBox, Priority.NEVER);
        progressBox.getChildren().setAll(progressBar);
        progressBox.visibleProperty().bind(model.ongoingProperty());

        startBtn = button(t(ACTION_START), Material2MZ.PLAY_ARROW, "large");
        startBtn.setOnAction(e -> start());

        stopBtn = button(t(ACTION_STOP), Material2MZ.STOP, "large");
        stopBtn.setOnAction(e -> stop());

        timeoutSpinner = new Spinner<>(100, 10_000, 500, 100);
        timeoutSpinner.setMinWidth(120);
        timeoutSpinner.setPrefWidth(120);

        HBox controlsBox = hbox(10, Pos.CENTER_LEFT, new Insets(5, 10, 10, 10));
        VBox.setVgrow(controlsBox, Priority.NEVER);
        controlsBox.getChildren().addAll(
                startBtn,
                stopBtn,
                timeoutSpinner,
                new Label(t(APICLIENT_SHOW_TIMEOUT_BETWEEN_REQUESTS))
        );

        getChildren().setAll(tabPane, progressBox, controlsBox);
        setId("api-client");
    }

    @Override
    public void initialize() {
        Component.propagateMouseEventsToParent(tabPane);

        startBtn.setDisable(true);
        startBtn.disableProperty().bind(BindUtils.or(
                model.selectedTemplateProperty().isNull(),
                model.ongoingProperty(),
                isBlank(model.csvTextProperty())
        ));

        stopBtn.disableProperty().bind(Bindings.not(model.ongoingProperty()));

        model.timeoutProperty().bind(timeoutSpinner.valueProperty());

        model.logStatProperty().addListener((obs, old, value) -> {
            if (value != null) { progressBar.setProgress(value.getProgress()); }
        });

        // refresh params table
        model.selectFirstTemplate();
    }

    private void start() {
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

        model.startCommand().execute();
        tabPane.getSelectionModel().selectLast();
    }

    private void stop() {
        model.stopCommand().execute();
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public ApiClientViewModel getViewModel() { return model; }

    @Override
    public Node getPrimaryFocusNode() { return settingsTab.csvText; }

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
