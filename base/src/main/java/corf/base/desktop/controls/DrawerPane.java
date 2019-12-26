package corf.base.desktop.controls;

import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;
import corf.base.desktop.Animations;
import corf.base.desktop.Overlay;

import java.util.Objects;
import java.util.function.Consumer;

public class DrawerPane extends StackPane implements Overlay {

    protected ScrollPane scrollPane;
    protected StackPane contentWrapper;
    protected Pos contentPos;
    protected Insets contentPadding = Insets.EMPTY;

    private final ReadOnlyBooleanWrapper onFrontProperty = new ReadOnlyBooleanWrapper(this, "onFront", false);
    private final Timeline fadeInTransition = Animations.fadeIn(this, Duration.millis(100));
    private final Timeline fadeOutTransition = Animations.fadeOut(this, Duration.millis(200));

    @SuppressWarnings("NullAway.Init")
    public DrawerPane() {
        super();

        createView();
    }

    private void createView() {
        contentWrapper = new StackPane();
        contentWrapper.getStyleClass().add("scrollable-content");
        contentWrapper.setAlignment(Pos.CENTER);

        scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(20000); // scroll pane won't work without height specified

        Consumer<Event> hideAndConsume = e -> {
            removeContent();
            toBack();
            e.consume();
        };

        // Hide overlay by pressing ESC. It only works when overlay or on of its
        // children has focus. That's why we requesting it in the toFront().
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) { hideAndConsume.accept(e); }
        });

        // Hide overlay by clicking outside the content area. Don't use MOUSE_CLICKED,
        // because it's the same as MOUSE_RELEASED event, thus it doesn't prevent cases
        // when user pressed moused inside the content and released outside it.
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            Pane content = getContent();
            if (e.getButton() != MouseButton.PRIMARY || content == null || isClickInArea(e, content)) { return; }

            boolean scrollBarClick = scrollPane.lookupAll(".scroll-bar").stream()
                    .filter(c -> c instanceof ScrollBar)
                    .anyMatch(c -> isClickInArea(e, (ScrollBar) c));

            if (!scrollBarClick) {
                hideAndConsume.accept(e);
            }
        });

        getChildren().add(scrollPane);
        getStyleClass().add("overlay");
    }

    @Override
    public @Nullable Pane getContent() {
        return FXHelpers.findChildByIndex(contentWrapper, 0, Pane.class);
    }

    @Override
    public void setContent(Pane content, @Nullable Pos pos, @Nullable Insets padding) {
        Objects.requireNonNull(content, "content");

        contentPos = Objects.requireNonNullElse(pos, Pos.CENTER);
        contentPadding = Objects.requireNonNullElse(padding, Insets.EMPTY);

        contentWrapper.getChildren().setAll(content);
        contentWrapper.setPadding(padding);
        StackPane.setAlignment(content, contentPos);

        scrollPane.setContent(contentWrapper);
    }

    @SuppressWarnings("ShortCircuitBoolean")
    private boolean isClickInArea(MouseEvent e, Region area) {
        return (e.getX() >= area.getLayoutX() & e.getX() <= area.getLayoutX() + area.getWidth())
                && (e.getY() >= area.getLayoutY() & e.getY() <= area.getLayoutY() + area.getHeight());
    }

    @Override
    public void removeContent() {
        contentWrapper.getChildren().clear();
    }

    public boolean contains(Pane pane) {
        return pane != null && !contentWrapper.getChildren().isEmpty() && Objects.equals(getContent(), pane);
    }

    @Override
    public void toFront() {
        if (onFrontProperty.get()) { return; }

        fadeInTransition.playFromStart();
        super.toFront();
        onFrontProperty.set(true);
    }

    @Override
    public void toBack() {
        if (!onFrontProperty.get()) { return; }

        // That's a bit risky, because what if the animation won't complete?
        // But sending overlay to back without waiting animation completion causes
        // an unpleasant flashing effect.
        fadeOutTransition.setOnFinished(e -> super.toBack());

        fadeOutTransition.playFromStart();
        onFrontProperty.set(false);
    }

    public ReadOnlyBooleanProperty onFrontProperty() {
        return onFrontProperty.getReadOnlyProperty();
    }
}
