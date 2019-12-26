package corf.desktop.layout.preferences;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import corf.base.desktop.controls.HorizontalForm;
import corf.base.desktop.controls.ModalDialog;
import corf.base.preferences.internal.ApplicationPreferences;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.Objects;

import static corf.base.i18n.I18n.t;

@Singleton
public class PreferencesDialogView extends ModalDialog
        implements View<PreferencesDialogView, PreferencesDialogViewModel>, Initializable {

    private static final int DIALOG_WIDTH = 700;

    ComboBox<Theme> themeChoice;

    Button applyBtn;
    Button closeBtn;

    private final PreferencesDialogViewModel model;

    @Inject
    @SuppressWarnings("NullAway.Init")
    public PreferencesDialogView(PreferencesDialogViewModel model) {
        super();

        this.model = model;

        setContent(createContent());
        setId("preferences-dialog");
    }

    private Content createContent() {
        var generalPane = createGeneralOptions();
        var proxyPane = new ProxyPreferencesPane(model);

        var body = new VBox();
        body.getChildren().addAll(
                generalPane,
                new Spacer(30, Orientation.VERTICAL),
                proxyPane
        );
        body.setPrefWidth(DIALOG_WIDTH);

        // == FOOTER ==

        applyBtn = new Button(t(DM.ACTION_APPLY));
        applyBtn.setDefaultButton(true);
        applyBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var footer = new HBox(
                Recommends.FORM_INLINE_SPACING,
                new Spacer(),
                applyBtn,
                closeBtn
        );

        return Content.create(t(DM.PREFERENCES), body, footer);
    }

    @Override
    public void init() {
        themeChoice.valueProperty().bindBidirectional(model.themeProperty());

        applyBtn.setOnAction(e -> model.applyCommand().run());
        closeBtn.setOnAction(e -> close());
    }

    @Override
    public PreferencesDialogView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public PreferencesDialogViewModel getViewModel() {
        return model;
    }

    private HorizontalForm createGeneralOptions() {
        themeChoice = new ComboBox<>();
        themeChoice.setPrefWidth(300);
        themeChoice.getItems().setAll(ApplicationPreferences.THEMES);
        themeChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Theme theme) {
                return theme != null ? theme.getName() : "";
            }

            @Override
            public Theme fromString(String name) {
                return ApplicationPreferences.THEMES.stream()
                        .filter(t -> Objects.equals(t.getName(), name))
                        .findFirst()
                        .orElse(new PrimerLight());
            }
        });

        var form = new HorizontalForm();
        form.setHgap(Recommends.FORM_HGAP);
        form.setVgap(Recommends.FORM_VGAP);

        form.add(t(DM.COLOR_THEME), themeChoice);

        return form;
    }
}
