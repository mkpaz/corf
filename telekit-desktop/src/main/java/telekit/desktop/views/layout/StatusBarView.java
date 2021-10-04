package telekit.desktop.views.layout;

import javafx.beans.binding.Bindings;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import telekit.base.desktop.mvvm.View;
import telekit.base.di.Initializable;
import telekit.controls.util.Containers;
import telekit.controls.util.Controls;

import javax.inject.Inject;
import javax.inject.Singleton;

import static telekit.base.i18n.I18n.t;
import static telekit.controls.util.Containers.horizontalSpacer;
import static telekit.desktop.i18n.DesktopMessages.SYSTEM_MSG_EXECUTING_TASKS;
import static telekit.desktop.views.layout.StatusBarViewModel.VAULT_UNLOCKED;
import static telekit.desktop.views.layout.StatusBarViewModel.VAULT_UNLOCK_FAILED;

@Singleton
public class StatusBarView extends HBox implements Initializable, View<StatusBarViewModel> {

    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    FontIcon vaultIcon;
    HBox progressBox;
    ProgressBar memoryBar;
    Text memoryStatus;

    private final StatusBarViewModel model;

    @Inject
    public StatusBarView(StatusBarViewModel model) {
        this.model = model;

        createView();
    }

    private void createView() {
        // security
        HBox securityBox = Containers.create(HBox::new, "security");
        securityBox.setAlignment(Pos.CENTER_LEFT);

        vaultIcon = Controls.fontIcon(Material2AL.LOCK, "vault-icon");

        securityBox.getChildren().add(vaultIcon);

        // tasks progress
        progressBox = Containers.create(HBox::new, "progress-tasks");
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setVisible(false); // no active tasks by default

        ProgressIndicator progressIndicator = Controls.create(ProgressIndicator::new);
        Label progressStatus = new Label(t(SYSTEM_MSG_EXECUTING_TASKS));

        progressBox.getChildren().addAll(progressIndicator, progressStatus);

        // memory indication
        StackPane memoryBox = Containers.create(StackPane::new, "memory");
        memoryBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(memoryBox, Priority.NEVER);

        memoryBar = new ProgressBar();
        memoryStatus = Controls.create(Text::new, "status");

        memoryBox.getChildren().addAll(memoryBar, memoryStatus);

        getChildren().addAll(securityBox, progressBox, horizontalSpacer(), memoryBox);
        HBox.setHgrow(this, Priority.ALWAYS);
        setAlignment(Pos.CENTER_LEFT);
        setId("status-bar");
    }

    @Override
    public void initialize() {
        // change vault icon when vault state changed
        model.vaultStateProperty().addListener((obs, old, value) -> {
            if (value == null) { return; }

            if (value.intValue() != VAULT_UNLOCKED) { vaultIcon.setIconCode(Material2AL.LOCK); }
            if (value.intValue() == VAULT_UNLOCKED) { vaultIcon.setIconCode(Material2AL.LOCK_OPEN); }

            pseudoClassStateChanged(ERROR, value.intValue() == VAULT_UNLOCK_FAILED);
        });

        model.activeTasks().addListener(
                (SetChangeListener<String>) change -> progressBox.setVisible(model.activeTasks().size() > 0)
        );

        memoryBar.progressProperty().bind(Bindings.createDoubleBinding(
                () -> model.usedMemoryProperty().get() / model.totalMemoryProperty().get(),
                model.usedMemoryProperty())
        );
        memoryBar.setOnMouseClicked(e -> model.runGcCommand().execute());

        memoryStatus.textProperty().bind(Bindings.createStringBinding(
                () -> model.usedMemoryProperty().get() + " / " + model.totalMemoryProperty().get() + " M",
                model.usedMemoryProperty())
        );
        memoryStatus.setOnMouseClicked(e -> model.runGcCommand().execute());
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() {}

    @Override
    public StatusBarViewModel getViewModel() { return model; }

    @Override
    public Node getPrimaryFocusNode() { return null; }
}
