package org.telekit.ui.main;

import org.telekit.ui.Launcher;

import java.net.URL;
import java.util.Objects;

public enum Views {

    API_CLIENT("api_client/_root.fxml"),
    API_CLIENT_TEMPLATE("api_client/template_modal.fxml"),
    API_CLIENT_PARAM("api_client/param_modal.fxml"),
    BASE64_ENCODER("base64_encoder/_root.fxml"),
    IMPORT_FILE_BUILDER("import_file_builder/_root.fxml"),
    IMPORT_FILE_BUILDER_TEMPLATE("import_file_builder/template_modal.fxml"),
    IMPORT_FILE_BUILDER_PARAM("import_file_builder/param_modal.fxml"),
    IP_V4_CALCULATOR("ip_calculator/ipv4.fxml"),
    IP_V4_CONVERTER("ip_calculator/ipv4_converter.fxml"),
    PASSWORD_GENERATOR("password_generator/_root.fxml"),
    SEQUENCE_GENERATOR("sequence_generator/_root.fxml"),
    SS7_CIC_TABLE("ss7/cic_table.fxml"),
    SS7_SPC_CONVERTER("ss7/spc_converter.fxml"),
    TRANSLITERATOR("transliterator/_root.fxml"),
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

    Views(String path) {
        this.path = ASSETS_PATH + path;
    }
}