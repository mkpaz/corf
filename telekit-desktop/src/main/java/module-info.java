module telekit.desktop {

    requires telekit.base;
    requires telekit.controls;

    // not modularized dependencies
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;

    requires j2html;

    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;
    requires org.apache.commons.codec;

    // exports
    exports org.telekit.desktop.domain;
    opens org.telekit.desktop.domain;

    exports org.telekit.desktop.main;
    opens org.telekit.desktop.main;

    exports org.telekit.desktop;
    opens org.telekit.desktop;

    // tools
    exports org.telekit.desktop.tools;
    opens org.telekit.desktop.tools;

    exports org.telekit.desktop.tools.common;
    opens org.telekit.desktop.tools.common;

    exports org.telekit.desktop.tools.apiclient;
    opens org.telekit.desktop.tools.apiclient;

    exports org.telekit.desktop.tools.base64;
    opens org.telekit.desktop.tools.base64;

    exports org.telekit.desktop.tools.filebuilder;
    opens org.telekit.desktop.tools.filebuilder;

    exports org.telekit.desktop.tools.ipcalc;
    opens org.telekit.desktop.tools.ipcalc;

    exports org.telekit.desktop.tools.passgen;
    opens org.telekit.desktop.tools.passgen;

    exports org.telekit.desktop.tools.seqgen;
    opens org.telekit.desktop.tools.seqgen;

    exports org.telekit.desktop.tools.ss7;
    opens org.telekit.desktop.tools.ss7;

    exports org.telekit.desktop.tools.translit;
    opens org.telekit.desktop.tools.translit;
    exports org.telekit.desktop.i18n;
    opens org.telekit.desktop.i18n;
}