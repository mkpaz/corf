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

    exports org.telekit.ui.samples;
    opens org.telekit.ui.samples;

    // tools
    exports org.telekit.ui.tools;
    opens org.telekit.ui.tools;

    exports org.telekit.ui.tools.apiclient;
    opens org.telekit.ui.tools.apiclient;

    exports org.telekit.ui.tools.base64;
    opens org.telekit.ui.tools.base64;

    exports org.telekit.ui.tools.filebuilder;
    opens org.telekit.ui.tools.filebuilder;

    exports org.telekit.ui.tools.ipcalc;
    opens org.telekit.ui.tools.ipcalc;

    exports org.telekit.ui.tools.passgen;
    opens org.telekit.ui.tools.passgen;

    exports org.telekit.ui.tools.seqgen;
    opens org.telekit.ui.tools.seqgen;

    exports org.telekit.ui.tools.ss7;
    opens org.telekit.ui.tools.ss7;

    exports org.telekit.ui.tools.translit;
    opens org.telekit.ui.tools.translit;
}