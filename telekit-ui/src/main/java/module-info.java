import org.telekit.base.plugin.Plugin;

module telekit.ui {

    uses Plugin;

    requires telekit.base;

    // non modular dependencies
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.jetbrains.annotations;
    requires j2html;
    // requires SystemTray;
    // end of non modular dependencies

    exports org.telekit.ui.domain;
    opens org.telekit.ui.domain;

    exports org.telekit.ui.main;
    opens org.telekit.ui.main;

    exports org.telekit.ui;
    opens org.telekit.ui;

    exports org.telekit.ui.service;
    opens org.telekit.ui.service;

    // tools
    exports org.telekit.ui.tools;
    opens org.telekit.ui.tools;

    exports org.telekit.ui.tools.api_client;
    opens org.telekit.ui.tools.api_client;

    exports org.telekit.ui.tools.base64_encoder;
    opens org.telekit.ui.tools.base64_encoder;

    exports org.telekit.ui.tools.import_file_builder;
    opens org.telekit.ui.tools.import_file_builder;

    exports org.telekit.ui.tools.ip_calculator;
    opens org.telekit.ui.tools.ip_calculator;

    exports org.telekit.ui.tools.password_generator;
    opens org.telekit.ui.tools.password_generator;

    exports org.telekit.ui.tools.sequence_generator;
    opens org.telekit.ui.tools.sequence_generator;

    exports org.telekit.ui.tools.ss7;
    opens org.telekit.ui.tools.ss7;

    exports org.telekit.ui.tools.transliterator;
    opens org.telekit.ui.tools.transliterator;
}