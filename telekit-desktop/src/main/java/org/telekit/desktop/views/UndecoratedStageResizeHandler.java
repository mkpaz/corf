package org.telekit.desktop.views;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.Objects;

// FIXME: Resizing is too memory expensive (+100Mb RAM easily and memory doesn't return back to OS)
public class UndecoratedStageResizeHandler implements EventHandler<MouseEvent> {

    public static final int DEFAULT_BORDER_WIDTH = 4;

    protected final Stage stage;
    protected final Scene scene;
    protected final int borderWidth;
    protected final double padTop;
    protected final double padRight;
    protected final double padBottom;
    protected final double padLeft;

    protected Cursor cursor = Cursor.DEFAULT;
    protected double startX = 0;
    protected double startY = 0;
    protected double sceneOffsetX = 0;
    protected double sceneOffsetY = 0;

    private UndecoratedStageResizeHandler(Stage stage) {
        this(stage, DEFAULT_BORDER_WIDTH, Insets.EMPTY);
    }

    private UndecoratedStageResizeHandler(Stage stage, int borderWidth, Insets padding) {
        this.stage = stage;
        this.scene = stage.getScene();
        this.borderWidth = borderWidth;
        this.padTop = padding.getTop();
        this.padRight = padding.getRight();
        this.padBottom = padding.getBottom();
        this.padLeft = padding.getLeft();
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        EventType<? extends MouseEvent> mouseEventType = mouseEvent.getEventType();

        double mouseEventX = mouseEvent.getSceneX();
        double mouseEventY = mouseEvent.getSceneY();
        double viewWidth = stage.getWidth() - padLeft - padRight;
        double viewHeight = stage.getHeight() - padTop - padBottom;

        if (MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {

            if (mouseEventX < borderWidth + padLeft && mouseEventY < borderWidth + padTop) {
                cursor = Cursor.NW_RESIZE;
            } else if (mouseEventX < borderWidth + padLeft && mouseEventY > viewHeight - borderWidth + padBottom) {
                cursor = Cursor.SW_RESIZE;
            } else if (mouseEventX > viewWidth - borderWidth + padRight && mouseEventY < borderWidth + padTop) {
                cursor = Cursor.NE_RESIZE;
            } else if (mouseEventX > viewWidth - borderWidth + padRight && mouseEventY > viewHeight - borderWidth + padBottom) {
                cursor = Cursor.SE_RESIZE;
            } else if (mouseEventX < borderWidth + padLeft) {
                cursor = Cursor.W_RESIZE;
            } else if (mouseEventX > viewWidth - borderWidth + padRight) {
                cursor = Cursor.E_RESIZE;
            } else if (mouseEventY < borderWidth + padTop) {
                cursor = Cursor.N_RESIZE;
            } else if (mouseEventY > viewHeight - borderWidth + padBottom) {
                cursor = Cursor.S_RESIZE;
            } else {
                cursor = Cursor.DEFAULT;
            }
            scene.setCursor(cursor);
        } else if (MouseEvent.MOUSE_EXITED.equals(mouseEventType) || MouseEvent.MOUSE_EXITED_TARGET.equals(mouseEventType)) {
            scene.setCursor(Cursor.DEFAULT);
        } else if (MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
            startX = viewWidth - mouseEventX;
            startY = viewHeight - mouseEventY;
            sceneOffsetX = mouseEvent.getSceneX();
            sceneOffsetY = mouseEvent.getSceneY();
        } else if (MouseEvent.MOUSE_DRAGGED.equals(mouseEventType) && !Cursor.DEFAULT.equals(cursor)) {

            // FIXME Resizing window using NW, N, SW edges is also slightly moves it
            if (!Cursor.W_RESIZE.equals(cursor) && !Cursor.E_RESIZE.equals(cursor)) {
                double minHeight = stage.getMinHeight() > (borderWidth * 2) ? stage.getMinHeight() : (borderWidth * 2);

                if (Cursor.NW_RESIZE.equals(cursor) || Cursor.N_RESIZE.equals(cursor) || Cursor.NE_RESIZE.equals(cursor)) {
                    if (stage.getHeight() > minHeight || mouseEventY < 0) {
                        double height = stage.getY() - mouseEvent.getScreenY() + stage.getHeight() + sceneOffsetY;
                        double y = mouseEvent.getScreenY() - sceneOffsetY;
                        stage.setHeight(height);
                        stage.setY(y);
                    }
                } else {
                    if (stage.getHeight() > minHeight || mouseEventY + startY - stage.getHeight() > 0) {
                        stage.setHeight(mouseEventY + startY + padBottom + padTop);
                    }
                }
            }

            if (!Cursor.N_RESIZE.equals(cursor) && !Cursor.S_RESIZE.equals(cursor)) {
                double minWidth = stage.getMinWidth() > (borderWidth * 2) ? stage.getMinWidth() : (borderWidth * 2);
                if (Cursor.NW_RESIZE.equals(cursor) || Cursor.W_RESIZE.equals(cursor) || Cursor.SW_RESIZE.equals(cursor)) {
                    if (stage.getWidth() > minWidth || mouseEventX < 0) {
                        double width = stage.getX() - mouseEvent.getScreenX() + stage.getWidth() + sceneOffsetX;
                        double x = mouseEvent.getScreenX() - sceneOffsetX;

                        stage.setWidth(width);
                        stage.setX(x);
                    }
                } else {
                    if (stage.getWidth() > minWidth || mouseEventX + startX - stage.getWidth() > 0) {
                        stage.setWidth(mouseEventX + startX + padLeft + padRight);
                    }
                }
            }
        }
    }

    public static void attach(Stage stage, int borderWidth, Insets padding) {
        Scene scene = Objects.requireNonNull(stage.getScene(), "Scene must not be null");
        UndecoratedStageResizeHandler resizeHandler = new UndecoratedStageResizeHandler(stage, borderWidth, padding);
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeHandler);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeHandler);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeHandler);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeHandler);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeHandler);
    }
}