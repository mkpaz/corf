package corf.desktop.tools.common.ui;

import atlantafx.base.controls.Popover;
import atlantafx.base.controls.Popover.ArrowLocation;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.jetbrains.annotations.Nullable;
import corf.base.common.Lazy;
import corf.base.desktop.controls.FXHelpers;
import corf.desktop.i18n.DM;

import static corf.base.i18n.I18n.t;

public class DescriptionText extends TextFlow {

    private static final int POPOVER_WIDTH = 600;

    private final StringProperty text = new SimpleStringProperty();
    private final int overrunCount;

    private final Text displayedText = new Text();
    private final Text overrunIndicator = new Text("...");
    private final Hyperlink actionLink = new Hyperlink(t(DM.MORE));
    private final Lazy<Popover> popoverDialog;

    public DescriptionText(int overrunCount) {
        super();

        this.overrunCount = overrunCount;

        overrunIndicator.setOpacity(0.5);

        popoverDialog = new Lazy<>(() -> {
            var popover = new Popover();
            popover.setTitle(t(DM.DESCRIPTION));
            popover.setArrowLocation(ArrowLocation.TOP_CENTER);

            var text = new Text();

            var textFlow = new TextFlow(text);
            textFlow.setPrefWidth(POPOVER_WIDTH);

            popover.setContentNode(textFlow);

            return popover;
        });

        FXHelpers.setManaged(overrunIndicator, false);
        FXHelpers.setManaged(actionLink, false);
        getChildren().setAll(displayedText, overrunIndicator, actionLink);

        init();
    }

    private void init() {
        text.addListener((obs, old, val) -> {
            boolean overrun = val != null && val.length() > overrunCount;
            displayedText.setText(overrun ? val.substring(0, overrunCount) : val);
            FXHelpers.setManaged(overrunIndicator, overrun);
            FXHelpers.setManaged(actionLink, overrun);
        });

        actionLink.setOnAction(e -> {
            Popover popover = popoverDialog.get();
            TextFlow textFlow = (TextFlow) popover.getContentNode();
            ((Text) textFlow.getChildren().get(0)).setText(text.get());
            popover.show(actionLink);
        });
    }

    public @Nullable String getText() {
        return text.get();
    }

    public void setText(@Nullable String text) {
        this.text.set(text);
    }

    public StringProperty textProperty() {
        return text;
    }
}
