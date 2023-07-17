package corf.desktop.tools.ipcalc;

import backbonefx.di.Initializable;
import backbonefx.mvvm.View;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import corf.base.common.Lazy;
import corf.base.desktop.Focusable;
import corf.base.desktop.Overlay;
import corf.base.desktop.RegexUnaryOperator;
import corf.base.net.IPv4Host;
import corf.base.desktop.ExtraStyles;
import corf.desktop.i18n.DM;
import corf.desktop.layout.Recommends;

import java.util.regex.Pattern;

import static atlantafx.base.theme.Styles.*;
import static javafx.scene.control.TabPane.TabClosingPolicy;
import static corf.base.i18n.I18n.t;
import static corf.desktop.tools.ipcalc.IPv4CalcViewModel.*;

public final class IPv4CalcView extends VBox implements View<IPv4CalcView, IPv4CalcViewModel>,
        Initializable,
        Focusable {

    static final int TAB_HEIGHT = 600;
    private static final int TOOL_WIDTH = 800;

    Button ipConverterBtn;
    TextField ipText;
    ComboBox<NetworkInfo> netmaskChoice;
    TabPane tabs;

    private final IPv4CalcViewModel model;
    private final Overlay overlay;
    private final Lazy<ConverterDialog> converterDialog = new Lazy<>(this::createConverterDialog);

    @Inject
    public IPv4CalcView(IPv4CalcViewModel model, Overlay overlay) {
        this.model = model;
        this.overlay = overlay;

        createView();
    }

    private void createView() {
        // == INPUT FORM ==

        var titleLabel = new Label(t(DM.IP_ADDRESS));
        titleLabel.getStyleClass().add(TITLE_4);

        ipConverterBtn = new Button("", new FontIcon(Material2OutlinedMZ.TRANSFORM));
        ipConverterBtn.getStyleClass().addAll(BUTTON_ICON, LEFT_PILL, ACCENT);
        ipConverterBtn.setTooltip(new Tooltip(t(DM.IPV4CALC_IP_ADDRESS_CONVERTER)));

        ipText = new TextField();
        ipText.setPromptText("127.0.0.1");
        ipText.getStyleClass().addAll(ExtraStyles.MONOSPACE, CENTER_PILL);
        ipText.setMaxWidth(Double.MAX_VALUE);
        ipText.setAlignment(Pos.CENTER);
        HBox.setHgrow(ipText, Priority.ALWAYS);

        netmaskChoice = new ComboBox<>(FXCollections.observableArrayList(NETMASKS));
        netmaskChoice.getStyleClass().addAll(ExtraStyles.MONOSPACE, RIGHT_PILL);
        netmaskChoice.setPrefWidth(250);
        netmaskChoice.setButtonCell(new NetmaskCell());
        netmaskChoice.setCellFactory(property -> new NetmaskCell());

        var formBox = new HBox(ipConverterBtn, ipText, netmaskChoice);

        // == TABS ==
        tabs = new TabPane();
        tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().setAll(
                new HostInfoTab(model),
                new NetworkSplitTab(model),
                new NetmasksTab(model)
        );
        VBox.setVgrow(tabs, Priority.ALWAYS);

        // == ROOT ==

        getChildren().setAll(
                new VBox(Recommends.CAPTION_MARGIN, titleLabel, formBox),
                tabs
        );
        setMinWidth(TOOL_WIDTH);
        setMaxWidth(TOOL_WIDTH);
        setAlignment(Pos.TOP_LEFT);
        setSpacing(Recommends.CONTENT_SPACING);
        setPadding(Recommends.TOOL_PADDING);
        setId("ipv4-calculator");
    }

    @Override
    public void init() {
        tabs.setTabMinWidth(TOOL_WIDTH / 3.0 - 60);
        tabs.setTabMaxWidth(TOOL_WIDTH / 3.0 - 60);

        ipConverterBtn.setOnAction(e -> showConverterDialog());

        ipText.setTextFormatter(RegexUnaryOperator.createTextFormatter(Pattern.compile(IPv4Host.PATTERN)));
        ipText.textProperty().bindBidirectional(model.ipv4StringProperty());

        netmaskChoice.valueProperty().bindBidirectional(model.netmaskInfoProperty());

        // set initial data
        ipText.setText(DEFAULT_IP);
        netmaskChoice.getSelectionModel().select(32 - DEFAULT_NETMASK);
    }

    @Override
    public IPv4CalcView getRoot() {
        return this;
    }

    @Override
    public void reset() { }

    @Override
    public IPv4CalcViewModel getViewModel() {
        return model;
    }

    @Override
    public Node getPrimaryFocusNode() {
        return ipText;
    }

    private void showConverterDialog() {
        var dialog = converterDialog.get();
        dialog.setHost(model.ipv4StringProperty().get());
        overlay.show(dialog, Pos.TOP_CENTER, Recommends.MODAL_WINDOW_MARGIN);
    }

    private ConverterDialog createConverterDialog() {
        var dialog = new ConverterDialog();
        dialog.setOnCommit(ipStr -> {
            if (IPv4Host.isValidString(ipStr)) {
                ipText.setText(ipStr);
            }
            overlay.hide();
        });
        dialog.setOnCloseRequest(overlay::hide);

        return dialog;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class NetmaskCell extends ListCell<NetworkInfo> {

        private final Label prefixLabel;
        private final Label hostLabel;

        public NetmaskCell() {
            prefixLabel = new Label();

            hostLabel = new Label();
            hostLabel.setGraphic(prefixLabel);
            hostLabel.setGraphicTextGap(Recommends.FORM_HGAP);
            hostLabel.getStyleClass().add(TEXT_SUBTLE);
        }

        @Override
        protected void updateItem(NetworkInfo network, boolean empty) {
            super.updateItem(network, empty);

            if (network == null || empty) {
                setGraphic(null);
            } else {
                prefixLabel.setText("/" + network.getPrefixLength());
                hostLabel.setText(network.getNetmaskAsDecimal());
                setGraphic(hostLabel);
            }
        }
    }
}
