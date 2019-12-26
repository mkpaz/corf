package corf.base.desktop.controls;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import corf.base.desktop.ModalController;

import java.util.Objects;

import static atlantafx.base.theme.Styles.*;

public abstract class ModalDialog extends AnchorPane implements ModalController {

    protected Button topCloseBtn;
    protected Runnable closeHandler;

    @SuppressWarnings("NullAway.Init")
    public ModalDialog() {
        super();
        createView();
    }

    protected void createView() {
        topCloseBtn = new Button("", new FontIcon(Material2AL.CLOSE));
        topCloseBtn.getStyleClass().addAll(BUTTON_CIRCLE, FLAT);
        topCloseBtn.setOnAction(e -> close());
        AnchorPane.setTopAnchor(topCloseBtn, 5d);
        AnchorPane.setRightAnchor(topCloseBtn, 5d);

        // this guarantees client will use correct width and height
        setMinWidth(Region.USE_PREF_SIZE);
        setMaxWidth(Region.USE_PREF_SIZE);
        setMinHeight(Region.USE_PREF_SIZE);
        setMaxHeight(Region.USE_PREF_SIZE);

        getStyleClass().add("modal-dialog");
        getChildren().setAll(topCloseBtn);
    }

    @Override
    public Runnable getOnCloseRequest() {
        return closeHandler;
    }

    @Override
    public void setOnCloseRequest(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    protected void setContent(Content content) {
        var root = new VBox();
        root.getStyleClass().add("content");
        AnchorPane.setTopAnchor(root, 0d);
        AnchorPane.setLeftAnchor(root, 0d);

        if (content.header() != null) {
            content.header().getStyleClass().add("header");
            root.getChildren().add(content.header());
        }

        content.body().getStyleClass().add("body");
        root.getChildren().add(content.body());

        if (content.footer() != null) {
            content.footer().getStyleClass().add("footer");
            root.getChildren().add(content.footer());
        }

        getChildren().add(0, root);
    }

    ///////////////////////////////////////////////////////////////////////////

    public record Content(@Nullable Node header,
                          Node body,
                          @Nullable Node footer) {

        public Content {
            Objects.requireNonNull(body, "body");
        }

        public static Content create(String title, Node body, @Nullable Node footer) {
            var titleLabel = new Label(title);
            titleLabel.getStyleClass().add(TITLE_4);

            return new Content(new HBox(titleLabel), body, footer);
        }
    }
}
