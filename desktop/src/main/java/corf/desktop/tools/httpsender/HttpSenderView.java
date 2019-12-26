package corf.desktop.tools.httpsender;

import atlantafx.base.controls.Spacer;
import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.common.Lazy;
import corf.base.desktop.Focusable;
import corf.base.desktop.Observables;
import corf.base.desktop.Overlay;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;

public final class HttpSenderView extends VBox implements View<HttpSenderView, HttpSenderViewModel>,
        Initializable,
        Focusable {

    static final int LEFT_AREA_WIDTH = 400;
    static final int TOOL_MAX_WIDTH = 1440;

    StackPane tabs;
    ToggleButton templateToggle;
    ToggleButton logToggle;

    TemplateTab templateTab;
    LogTab logTab;
    ProgressBar progressBar;
    Button startBtn;
    Button stopBtn;

    private final HttpSenderViewModel model;
    private final Overlay overlay;
    private final Lazy<StartDialog> startDialog;

    @Inject
    public HttpSenderView(HttpSenderViewModel model, Overlay overlay) {
        this.model = model;
        this.overlay = overlay;

        this.startDialog = new Lazy<>(() -> {
            var dialog = new StartDialog(model);
            dialog.setOnCloseRequest(this::hideOverlay);
            return dialog;
        });

        createView();
    }

    private void createView() {
        // == TABS ==

        var toggleGroup = new ToggleGroup();

        templateToggle = new ToggleButton(t(DM.SETTINGS));
        templateToggle.getStyleClass().addAll(LEFT_PILL, SMALL);
        templateToggle.setToggleGroup(toggleGroup);
        templateToggle.setSelected(true);
        templateToggle.setPrefWidth(150);

        logToggle = new ToggleButton(t(DM.LOG));
        logToggle.getStyleClass().addAll(RIGHT_PILL, SMALL);
        logToggle.setToggleGroup(toggleGroup);
        logToggle.setPrefWidth(150);

        var tabToggleBox = new HBox(templateToggle, logToggle);
        tabToggleBox.setAlignment(Pos.CENTER);

        templateTab = new TemplateTab(this);

        logTab = new LogTab(this);

        tabs = new StackPane();
        tabs.getChildren().setAll(logTab, templateTab);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        // == ACTIONS ==

        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add(SMALL);

        var startIcon = new FontIcon(Material2OutlinedMZ.PLAY_CIRCLE_OUTLINE);
        startIcon.getStyleClass().addAll(SUCCESS);

        startBtn = new Button(t(DM.ACTION_START), startIcon);
        startBtn.getStyleClass().add(LARGE);
        startBtn.setMinWidth(150);

        var stopIcon = new FontIcon(Material2OutlinedMZ.STOP_CIRCLE);
        stopIcon.getStyleClass().addAll(DANGER);

        stopBtn = new Button(t(DM.ACTION_STOP), stopIcon);
        stopBtn.getStyleClass().add(LARGE);

        var actionsBox = new HBox(Recommends.FORM_INLINE_SPACING, startBtn, stopBtn);
        actionsBox.setAlignment(Pos.CENTER);

        setMaxWidth(TOOL_MAX_WIDTH);
        setPadding(Recommends.TOOL_PADDING);
        getChildren().setAll(
                tabToggleBox,
                new Spacer(10, Orientation.VERTICAL),
                tabs,
                new Spacer(Recommends.CONTENT_SPACING, Orientation.VERTICAL),
                new VBox(5, progressBar, actionsBox)

        );
        setId("http-sender");
    }

    @Override
    public void init() {
        templateToggle.selectedProperty().addListener((obs, old, val) -> {
            if (val) { templateTab.toFront(); }
        });

        logToggle.selectedProperty().addListener((obs, old, val) -> {
            if (val) { logTab.toFront(); }
        });

        startBtn.setOnAction(e -> start());
        startBtn.setDisable(true);
        startBtn.disableProperty().bind(Observables.or(
                model.selectedTemplateProperty().isNull(),
                model.ongoingProperty(),
                Observables.isBlank(model.csvTextProperty())
        ));

        stopBtn.setOnAction(e -> stop());
        stopBtn.disableProperty().bind(Bindings.not(model.ongoingProperty()));
        stopBtn.minWidthProperty().bind(startBtn.widthProperty());

        progressBar.visibleProperty().bind(model.ongoingProperty());

        model.logStatProperty().addListener((obs, old, val) -> {
            if (val != null) { progressBar.setProgress(val.getProgress()); }
        });

        // switch to log view upon starting new task
        model.ongoingProperty().addListener((obs, old, val) -> {
            if (val) { logToggle.setSelected(true); }
        });
    }

    @Override
    public HttpSenderView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public HttpSenderViewModel getViewModel() {
        return model;
    }

    @Override
    public Node getPrimaryFocusNode() {
        return templateTab.paramsCard.csvText;
    }

    private void start() {
        model.setTemplateParams(templateTab.paramsCard.getEditedParams());
        var dialog = startDialog.get();
        dialog.prepare();
        showOverlay(dialog);
    }

    private void stop() {
        model.stopCommand().run();
    }

    ///////////////////////////////////////////////////////////////////////////

    Overlay getOverlay() {
        return overlay;
    }

    void showOverlay(Pane content) {
        overlay.show(content, Pos.TOP_CENTER, Recommends.MODAL_WINDOW_MARGIN);
    }

    void hideOverlay() {
        overlay.hide();
    }
}
