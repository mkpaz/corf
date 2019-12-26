package corf.desktop.tools.httpsender;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import corf.base.common.NumberUtils;
import corf.base.desktop.OS;
import corf.base.desktop.controls.ModalDialog;
import corf.base.net.HttpClient;
import corf.base.text.LineSeparator;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static atlantafx.base.theme.Styles.*;
import static corf.base.i18n.I18n.t;
import static corf.base.desktop.ExtraStyles.MONOSPACE;

final class LogRecordDialog extends ModalDialog {

    private static final int DIALOG_WIDTH = 800;

    Label methodLabel;
    Label uriLabel;
    Button copyUriBtn;
    MessageInfo requestInfo;

    FontIcon statusIcon;
    Label statusLabel;
    Label durationLabel;
    MessageInfo responseInfo;

    HBox navBox;
    Button prevBtn;
    Button nextBtn;
    Button closeBtn;

    private final HttpSenderViewModel model;
    private final IntegerProperty indexProperty = new SimpleIntegerProperty(-1);

    @SuppressWarnings("NullAway.Init")
    public LogRecordDialog(HttpSenderViewModel model) {
        super();

        this.model = model;

        setContent(createContent());
        init();
    }

    private Content createContent() {
        methodLabel = new Label();
        methodLabel.getStyleClass().addAll(TEXT_BOLD, TEXT_MUTED);

        uriLabel = new Label();
        uriLabel.getStyleClass().add(MONOSPACE);
        uriLabel.setGraphic(new FontIcon(Material2OutlinedMZ.PUBLIC));
        uriLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        copyUriBtn = new Button("", new FontIcon(Material2OutlinedAL.CONTENT_COPY));
        copyUriBtn.getStyleClass().add(FLAT);

        var uriBox = new HBox(methodLabel, uriLabel, new Spacer(), copyUriBtn);
        uriBox.setSpacing(10);
        uriBox.setAlignment(Pos.CENTER_LEFT);

        requestInfo = new MessageInfo();
        requestInfo.setMinHeight(250);
        requestInfo.setMaxHeight(250);

        statusIcon = new FontIcon(Material2AL.BRIGHTNESS_1);

        statusLabel = new Label();
        statusLabel.setGraphic(statusIcon);
        statusLabel.setGraphicTextGap(10);

        durationLabel = new Label();

        var statusBox = new HBox(20, statusLabel, durationLabel);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        responseInfo = new MessageInfo();
        responseInfo.setMinHeight(250);
        responseInfo.setMaxHeight(250);

        var body = new VBox();
        body.getChildren().addAll(
                new VBox(10, uriBox, requestInfo),
                new VBox(10, statusBox, responseInfo)
        );
        body.setSpacing(Recommends.CONTENT_SPACING);
        body.setPrefWidth(DIALOG_WIDTH);

        // == FOOTER ==

        prevBtn = new Button("", new FontIcon(Material2MZ.NAVIGATE_BEFORE));
        prevBtn.getStyleClass().add(FLAT);

        nextBtn = new Button("", new FontIcon(Material2MZ.NAVIGATE_NEXT));
        nextBtn.getStyleClass().add(FLAT);

        navBox = new HBox(Recommends.FORM_INLINE_SPACING, prevBtn, nextBtn);
        navBox.setAlignment(Pos.CENTER_LEFT);

        closeBtn = new Button(t(DM.ACTION_CLOSE));
        closeBtn.setMinWidth(Recommends.FORM_BUTTON_WIDTH);

        var footer = new HBox(navBox, new Spacer(), closeBtn);

        return Content.create(t(DM.DETAILS), body, footer);
    }

    private void init() {
        indexProperty.addListener((obs, old, val) -> {
            if (val != null && val.intValue() >= 0) {
                var entry = getRecords().get(val.intValue());
                var req = entry.getHttpRequest();
                var rsp = entry.getHttpResponse();

                uriLabel.setText(req.uri().toString());
                methodLabel.setText(req.method().toString());

                statusLabel.setText(entry.getStatusCode() + ", " + entry.getReasonPhrase());
                durationLabel.setText(String.format("%.3fs", entry.getDuration() / 1000.0));

                if (entry.succeeded()) {
                    Styles.activatePseudoClass(statusIcon, STATE_SUCCESS, STATE_WARNING, STATE_DANGER);
                    Styles.activatePseudoClass(statusLabel, STATE_SUCCESS, STATE_WARNING, STATE_DANGER);
                }
                if (entry.forwarded()) {
                    Styles.activatePseudoClass(statusIcon, STATE_WARNING, STATE_SUCCESS, STATE_DANGER);
                    Styles.activatePseudoClass(statusLabel, STATE_WARNING, STATE_SUCCESS, STATE_DANGER);
                }
                if (entry.failed()) {
                    Styles.activatePseudoClass(statusIcon, STATE_DANGER, STATE_SUCCESS, STATE_WARNING);
                    Styles.activatePseudoClass(statusLabel, STATE_DANGER, STATE_SUCCESS, STATE_WARNING);
                }

                requestInfo.setContent(req);
                responseInfo.setContent(rsp);
            } else {
                uriLabel.setText(null);
                methodLabel.setText(null);

                statusLabel.setText(null);
                durationLabel.setText(null);

                requestInfo.setContent((HttpClient.Request) null);
                responseInfo.setContent((HttpClient.Response) null);
            }
        });

        copyUriBtn.setOnAction(e -> OS.setClipboard(uriLabel.getText()));

        navBox.visibleProperty().bind(Bindings.size(getRecords()).greaterThan(1));

        prevBtn.setOnAction(e -> {
            if (!getRecords().isEmpty() && indexProperty.get() > 0) {
                indexProperty.set(indexProperty.get() - 1);
            }
        });
        prevBtn.disableProperty().bind(Bindings.createBooleanBinding(
                // Integer.lessThan() won't work properly, do not try to refactor
                () -> getRecords().isEmpty() || indexProperty.get() <= 0, indexProperty)
        );

        nextBtn.setOnAction(e -> {
            if (!getRecords().isEmpty() && indexProperty.get() < getRecords().size() - 1) {
                indexProperty.set(indexProperty.get() + 1);
            }
        });
        nextBtn.disableProperty().bind(Bindings.createBooleanBinding(
                // Integer.lessThan() won't work properly, do not try to refactor
                () -> getRecords().isEmpty() || indexProperty.get() >= getRecords().size() - 1, indexProperty)
        );

        closeBtn.setOnAction(e -> close());
    }

    @Override
    public void close() {
        super.close();
        indexProperty.set(-1);
        requestInfo.resetTabSelection();
        responseInfo.resetTabSelection();
    }

    void setRowIndex(int index) {
        indexProperty.set(NumberUtils.ensureRange(index, 0, getRecords().size() - 1));
    }

    private ObservableList<LogRecord> getRecords() {
        return model.getFilteredLog();
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final class MessageInfo extends VBox {

        TabPane tabPane;
        Label headersTabLabel;
        Label headersCountLabel;
        ListView<Entry<String, String>> headerList;
        TextArea bodyText;

        public MessageInfo() {
            super();

            var bodyTab = new Tab(t(DM.HTTP_SENDER_BODY));

            headersCountLabel = new Label();
            headersCountLabel.getStyleClass().addAll(TEXT_MUTED, TEXT_SMALL);
            headersCountLabel.setPadding(new Insets(-5, 0, 0, 0));

            headersTabLabel = new Label(t(DM.HTTP_SENDER_HEADERS));
            headersTabLabel.setGraphicTextGap(10);
            headersTabLabel.setContentDisplay(ContentDisplay.RIGHT);
            headersTabLabel.setGraphic(headersCountLabel);

            var headersTab = new Tab();
            headersTab.setGraphic(headersTabLabel);

            tabPane = new TabPane(bodyTab, headersTab);
            tabPane.getStyleClass().add(SMALL);
            tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
            tabPane.getSelectionModel().select(bodyTab);
            VBox.setVgrow(tabPane, Priority.ALWAYS);

            bodyText = new TextArea();
            bodyText.setEditable(false);
            bodyText.getStyleClass().addAll(MONOSPACE, ExtraStyles.NO_BG_INSETS);

            var bodyBox = new StackPane(bodyText);
            StackPane.setMargin(bodyBox, new Insets(Recommends.CAPTION_MARGIN, 0, 0, 0));
            bodyTab.setContent(bodyBox);

            headerList = new ListView<>();
            headerList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            headerList.getStyleClass().addAll(DENSE, Tweaks.EDGE_TO_EDGE);
            headerList.setCellFactory(c -> new HeaderListCell());
            headerList.setOnKeyPressed(e -> {
                if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(e)) {
                    List<Entry<String, String>> items = headerList.getSelectionModel().getSelectedItems();
                    if (items != null && !items.isEmpty()) {
                        var s = items.stream()
                                .map(entry -> entry.getKey() + ": " + entry.getValue())
                                .sorted()
                                .collect(Collectors.joining(LineSeparator.UNIX.getCharacters()));
                        OS.setClipboard(s);
                    }
                }
            });

            var headerListBox = new StackPane(headerList);
            StackPane.setMargin(headerListBox, new Insets(Recommends.CAPTION_MARGIN, 0, 0, 0));
            headersTab.setContent(headerListBox);

            setSpacing(Recommends.FORM_VGAP);
            getChildren().setAll(tabPane);
        }

        void setContent(HttpClient.@Nullable Request request) {
            if (request != null) {
                bodyText.setText(request.body());
                var headers = request.headers().entrySet().stream()
                        .sorted(Entry.comparingByKey())
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));
                headerList.setItems(headers);
                headersCountLabel.setText(String.valueOf(headers.size()));
            } else {
                bodyText.setText(null);
                headerList.setItems(FXCollections.emptyObservableList());
                headersCountLabel.setText(null);
            }
        }

        void setContent(HttpClient.@Nullable Response response) {
            if (response != null) {
                bodyText.setText(response.body());
                var headers = response.headers().entrySet().stream()
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));
                headerList.setItems(headers);
                headersCountLabel.setText(String.valueOf(headers.size()));
            } else {
                bodyText.setText(null);
                headerList.setItems(FXCollections.emptyObservableList());
                headersCountLabel.setText(null);
            }
        }

        void resetTabSelection() {
            tabPane.getSelectionModel().selectFirst();
        }
    }

    private static class HeaderListCell extends ListCell<Entry<String, String>> {

        private final HBox root;
        private final Label keyLabel;
        private final Label valueLabel;

        public HeaderListCell() {
            super();

            keyLabel = new Label();
            keyLabel.getStyleClass().add(TEXT_BOLD);

            valueLabel = new Label();

            root = new HBox(keyLabel, valueLabel);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setSpacing(10);
        }

        @Override
        protected void updateItem(Entry<String, String> entry, boolean empty) {
            super.updateItem(entry, empty);

            if (entry != null && !empty) {
                keyLabel.setText(entry.getKey() + ":");
                valueLabel.setText(entry.getValue());
                setGraphic(root);
            } else {
                keyLabel.setText(null);
                valueLabel.setText(null);
                setGraphic(null);
            }
        }
    }
}
