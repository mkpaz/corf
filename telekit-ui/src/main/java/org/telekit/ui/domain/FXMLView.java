package org.telekit.ui.domain;

import org.telekit.ui.Launcher;

import java.net.URL;
import java.util.Objects;

public enum FXMLView {

    // tools
    API_CLIENT("apiclient/_root.fxml"),
    API_CLIENT_TEMPLATE("apiclient/template.fxml"),
    API_CLIENT_PARAM("apiclient/param.fxml"),
    BASE64("base64/_root.fxml"),
    FILE_BUILDER("filebuilder/_root.fxml"),
    FILE_BUILDER_TEMPLATE("filebuilder/template.fxml"),
    FILE_BUILDER_PARAM("filebuilder/param.fxml"),
    IPV4_CALC("ipcalc/ipv4-calc.fxml"),
    IPV4_CONV("ipcalc/ipv4-conv.fxml"),
    PASS_GEN("passgen/_root.fxml"),
    SEQ_GEN("seqgen/_root.fxml"),
    SS7_CIC_TABLE("ss7/cic-table.fxml"),
    SS7_SPC_CONV("ss7/spc-conv.fxml"),
    TRANSLIT("translit/_root.fxml"),
    // main
    ABOUT("about.fxml"),
    PREFERENCES("preferences.fxml"),
    PLUGIN_MANAGER("plugin-manager.fxml"),
    MAIN_WINDOW("main-window.fxml");

    private static final String ASSETS_PATH = "/assets/ui/";

    private final String path;

    public String getPath() {
        return path;
    }

    public URL getLocation() {
        return Objects.requireNonNull(Launcher.getResource(path));
    }

    FXMLView(String path) {
        this.path = ASSETS_PATH + path;
    }
}