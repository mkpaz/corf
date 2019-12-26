package corf.desktop.tools.common.ui;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Popover;
import atlantafx.base.controls.Spacer;
import corf.base.Env;
import corf.base.common.KeyValue;
import corf.base.common.Lazy;
import corf.base.desktop.OS;
import corf.base.desktop.Overlay;
import corf.base.event.ActionEvent;
import corf.base.event.Events;
import corf.base.exception.AppException;
import corf.base.preferences.CompletionProvider;
import corf.base.preferences.CompletionRegistry;
import corf.base.preferences.KeyValueCompletionProvider;
import corf.desktop.EventID;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import corf.desktop.tools.common.Param;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;
import static corf.base.text.CSV.COMMA_OR_SEMICOLON;

public class ParamList extends ScrollPane {

    protected static final PseudoClass LAST = PseudoClass.getPseudoClass("last");
    protected static final PseudoClass EMPTY = PseudoClass.getPseudoClass("empty");

    protected VBox itemList;
    protected VBox placeholder;
    protected Hyperlink placeholderLink;
    protected Runnable editTemplateHandler;
    protected final Lazy<Popover> paramConfigDialog = new Lazy<>(this::createParamConfigDialog);
    protected final Lazy<ParamCompletionDialog> paramCompletionDialog = new Lazy<>(this::createParamCompletionDialog);

    protected final CompletionRegistry completionRegistry;
    protected final Overlay overlay;
    protected final BiConsumer<Param, CustomTextField> completionHandler = createCompletionHandler();

    @SuppressWarnings("NullAway.Init")
    public ParamList(CompletionRegistry completionRegistry, Overlay overlay) {
        super();

        this.completionRegistry = Objects.requireNonNull(completionRegistry, "completionRegistry");
        this.overlay = Objects.requireNonNull(overlay, "overlay");

        createView();
        init();
    }

    private void createView() {
        itemList = new VBox();
        itemList.setFillWidth(true);
        itemList.getStyleClass().add("items");

        var placeholderLabel = new Label(t(DM.TPL_NO_NAMED_PARAMS_IN_SELECTED_TEMPLATE));
        placeholderLabel.getStyleClass().add(TEXT_SUBTLE);
        placeholderLabel.setWrapText(true);
        placeholderLabel.setMaxWidth(Double.MAX_VALUE);

        placeholderLink = new Hyperlink(t(DM.TPL_EDIT_TEMPLATE));

        placeholder = new VBox();
        placeholder.getChildren().setAll(placeholderLabel, placeholderLink);
        placeholder.setAlignment(Pos.CENTER_LEFT);
        placeholder.setFillWidth(true);
        placeholder.getStyleClass().add("placeholder");

        setContent(itemList);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setFitToHeight(false);
        setFitToWidth(true);
        getStyleClass().add("param-list");
    }

    private void init() {
        itemList.getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(e -> {
                        if (e instanceof ParamItem item) {
                            item.dispose();
                        }
                    });
                }
            }
        });

        placeholderLink.setOnAction(e -> {
            if (editTemplateHandler != null) {
                editTemplateHandler.run();
            }
        });

        Events.listen(ActionEvent.class, e -> {
            if (e.matches(EventID.COMPLETION_UPDATE)) {
                updateParamCompletion();
            }
        });
    }

    public Set<Param> getEditedParams() {
        return itemList.getChildren().stream()
                .filter(e -> e instanceof ParamItem)
                .map(item -> ((ParamItem) item).getEditedParam())
                .collect(Collectors.toSet());
    }

    public void setItems(@Nullable Collection<Param> params) {
        // params can be null when no templates exist
        setItems(params != null && !params.isEmpty() ? params.toArray(Param[]::new) : new Param[0]);
    }

    public void clearItems() {
        setItems();
    }

    public void setEditTemplateHandler(Runnable handler) {
        this.editTemplateHandler = handler;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private API                                                           //
    ///////////////////////////////////////////////////////////////////////////

    private void setItems(Param... items) {
        ObservableList<Node> children = itemList.getChildren();
        boolean empty = items == null || items.length == 0;

        pseudoClassStateChanged(EMPTY, empty);
        if (empty) {
            children.clear();
            setContent(placeholder);
            return;
        } else {
            if (getContent() == placeholder) {
                setContent(itemList);
            }
        }

        // reuse existing VBox nodes
        List<Node> reusable = children.subList(0, Math.min(children.size(), items.length));
        int reusableSize = reusable.size(); // avoid ConcurrentModificationException

        for (int i = 0; i < items.length; i++) {
            Param param = items[i];
            ParamItem item;

            if (i <= reusableSize - 1) {
                item = ((ParamItem) reusable.get(i));
            } else {
                item = new ParamItem();
                children.add(item);
            }

            item.setParam(param, doesParamSupportCompletion(param));
            item.setToggleCompletionHandler(completionHandler);
            item.pseudoClassStateChanged(LAST, i == items.length - 1);
        }

        // remove unused cells, if any
        if (children.size() > items.length) {
            children.remove(items.length, children.size());
        }
    }

    @SuppressWarnings("UnnecessaryLambda")
    private BiConsumer<Param, CustomTextField> createCompletionHandler() {
        return (param, valueText) -> {
            boolean supportsCompletion = doesParamSupportCompletion(param);

            if (!supportsCompletion) {
                Popover popover = paramConfigDialog.get();
                popover.setUserData(param.getName());
                popover.show(valueText.getLeft());
                return;
            }

            ParamCompletionDialog dialog = paramCompletionDialog.get();
            dialog.setData(
                    getParamCompletion(param),
                    KeyValueCompletionProvider.resolve(Env.AUTOCOMPLETE_DIR, param.getName())
            );
            dialog.setCommitHandler(kv -> {
                if (kv != null) {
                    valueText.setText(kv.getValue());
                }
                dialog.close();
            });
            overlay.show(dialog, Pos.TOP_CENTER, Recommends.MODAL_WINDOW_MARGIN);
        };
    }

    private void updateParamCompletion() {
        itemList.getChildren().forEach(e -> {
            if (e instanceof ParamItem item) {
                item.updateParamCompletion(doesParamSupportCompletion(item.getEditedParam()));
            }
        });
    }

    private List<KeyValue<String, String>> getParamCompletion(Param param) {
        KeyValueCompletionProvider provider = getCompletionProvider(param);
        return provider != null ? new ArrayList<>(provider.find(e -> true)) : Collections.emptyList();
    }

    private @Nullable KeyValueCompletionProvider getCompletionProvider(Param param) {
        CompletionProvider<?> provider = completionRegistry.getProviderFor(param.getName()).orElse(null);
        return provider instanceof KeyValueCompletionProvider value ? value : null;
    }

    private boolean doesParamSupportCompletion(Param param) {
        return param.getType() == Param.Type.CONSTANT
                && StringUtils.isNotBlank(param.getName())
                && completionRegistry.containsKey(param.getName());
    }

    private Popover createParamConfigDialog() {
        var popover = new Popover();
        popover.setDetachable(false);

        var actionButton = new Button(t(DM.ACTION_CONFIGURE));
        actionButton.getStyleClass().add(SUCCESS);
        actionButton.setOnAction(e -> {
            if (popover.getUserData() instanceof String paramName) {
                createAndOpenCompletionFile(paramName);
                popover.hide();
            }
        });

        var content = new VBox(Recommends.FORM_VGAP);
        content.getChildren().setAll(
                new Text("This param is not configured to show suggestions."),
                actionButton
        );

        popover.setContentNode(content);
        popover.setArrowLocation(Popover.ArrowLocation.TOP_CENTER);

        return popover;
    }

    private void createAndOpenCompletionFile(String paramName) {
        try {
            Path path = KeyValueCompletionProvider.createTemplateFile(Env.AUTOCOMPLETE_DIR, paramName);
            EventQueue.invokeLater(() -> OS.open(path.toFile()));
        } catch (IOException e) {
            throw new AppException(t(DM.MSG_GENERIC_IO_ERROR), e);
        }
    }

    private ParamCompletionDialog createParamCompletionDialog() {
        var dialog = new ParamCompletionDialog();
        dialog.setOnCloseRequest(overlay::hide);
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class ParamItem extends HBox {

        private static final int COL_WIDTH = 180;

        private final Label nameText;
        private final Text typeText;

        private final Text autoText = new Text(" auto");
        private final Label exampleLabel;
        private final VBox rightBox;
        private final CustomTextField valueText = new CustomTextField();
        private final ComboBox<String> valueChoice = new ComboBox<>();
        private final FontIcon completionIcon = new FontIcon(Material2OutlinedAL.EDIT);

        private @Nullable Param param = null;
        private @Nullable BiConsumer<Param, CustomTextField> toggleCompletionHandler = null;

        public ParamItem() {
            // == LEFT ==

            nameText = new Label();
            nameText.setTextOverrun(OverrunStyle.ELLIPSIS);

            typeText = new Text();
            typeText.getStyleClass().addAll(TEXT_SUBTLE, TEXT_SMALL);

            var leftBox = new VBox(nameText, typeText);
            leftBox.setAlignment(Pos.CENTER_LEFT);
            leftBox.setMinWidth(COL_WIDTH);
            leftBox.setMaxWidth(COL_WIDTH);

            // == RIGHT ==

            var editButton = new Button("", completionIcon);
            editButton.getStyleClass().addAll("edit-button", FLAT);
            editButton.setCursor(Cursor.HAND);
            editButton.setTooltip(new Tooltip(t(DM.TPL_SELECT_VALUE_FROM_THE_LIST)));
            editButton.setOnAction(event -> {
                if (toggleCompletionHandler != null) {
                    toggleCompletionHandler.accept(getEditedParam(), valueText);
                }
            });

            valueText.setLeft(editButton);

            valueChoice.setMaxWidth(Double.MAX_VALUE);

            exampleLabel = new Label();
            exampleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            exampleLabel.getStyleClass().addAll(TEXT_SUBTLE, TEXT_SMALL);

            rightBox = new VBox();
            rightBox.setAlignment(Pos.CENTER_LEFT);
            rightBox.setMinWidth(COL_WIDTH);
            rightBox.setMaxWidth(COL_WIDTH);

            // ~

            getChildren().setAll(leftBox, new Spacer(), rightBox);
            getStyleClass().add("item");
        }

        public void setToggleCompletionHandler(@Nullable BiConsumer<Param, CustomTextField> handler) {
            this.toggleCompletionHandler = handler;
        }

        public Param getEditedParam() {
            Objects.requireNonNull(param, "Invalid flow, param value isn't set.");

            Param p = param.copy();
            if (!p.isAutoGenerated()) {
                if (param.getType() == Param.Type.CHOICE) {
                    p.setValue(StringUtils.trim(valueChoice.getValue()));
                } else {
                    p.setValue(StringUtils.trim(valueText.getText()));
                }
            }

            return p;
        }

        public void setParam(Param param, boolean supportsCompletion) {
            this.param = Objects.requireNonNull(param, "param");

            nameText.setText(param.getName());
            typeText.setText(param.getType().toString());
            updateParamCompletion(supportsCompletion);

            if (!param.isAutoGenerated()) {
                setConstantParam(param);
            } else {
                setAutogeneratedParam(param);
            }
        }

        private void setConstantParam(Param param) {
            if (param.getType() == Param.Type.CHOICE) {
                rightBox.getChildren().setAll(valueChoice);

                if (StringUtils.isNotBlank(param.getOption())) {
                    String[] items = param.getOption().split(COMMA_OR_SEMICOLON);
                    valueChoice.setItems(FXCollections.observableArrayList(items));
                    if (items.length > 0) {
                        valueChoice.setValue(items[0]);
                    }
                }

                return;
            }

            rightBox.getChildren().setAll(valueText);
            valueText.setText(null);
        }

        private void setAutogeneratedParam(Param param) {
            rightBox.getChildren().setAll(autoText, exampleLabel);
            // space is for artificial padding to align with the text field
            exampleLabel.setText(" " + param.resolve().getValue());
        }

        public void updateParamCompletion(boolean supportsCompletion) {
            completionIcon.setIconCode(supportsCompletion ? Material2OutlinedAL.LIGHTBULB : Material2OutlinedAL.EDIT);
        }

        public void dispose() {
            toggleCompletionHandler = null;
            param = null;
        }
    }
}
