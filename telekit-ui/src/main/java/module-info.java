module telekit.ui {

    requires telekit.base;

    // not modularized dependencies
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.jetbrains.annotations;
    requires j2html;

    requires org.fxmisc.richtext;
    requires flowless;
    requires reactfx;
    requires undofx;
    requires wellbehavedfx;
    requires org.apache.commons.codec;
    // end of ot modularized dependencies

    exports org.telekit.ui.domain;
    opens org.telekit.ui.domain;

    exports org.telekit.ui.main;
    opens org.telekit.ui.main;

    exports org.telekit.ui;
    opens org.telekit.ui;

    exports org.telekit.ui.test;
    opens org.telekit.ui.test;

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