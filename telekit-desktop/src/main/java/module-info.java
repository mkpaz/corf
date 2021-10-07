module telekit.desktop {

    requires telekit.base;
    requires telekit.controls;

    // not modularized dependencies
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;
    requires com.j2html;
    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;

    // only for dev mode to check JCL bridge is working
    requires org.apache.commons.logging;

    // only to log JVM options
    requires java.management;

    // exports
    exports telekit.desktop;
    exports telekit.desktop.event;
    exports telekit.desktop.views.layout;
    exports telekit.desktop.views.system;
    exports telekit.desktop.startup;
    exports telekit.desktop.startup.config;
    exports telekit.desktop.views;

    exports telekit.desktop.i18n;
    opens telekit.desktop.i18n;

    // tools
    exports telekit.desktop.tools.common;
    opens telekit.desktop.tools.common;

    exports telekit.desktop.tools.apiclient;
    opens telekit.desktop.tools.apiclient;

    exports telekit.desktop.tools.base64;
    opens telekit.desktop.tools.base64;

    exports telekit.desktop.tools.filebuilder;
    opens telekit.desktop.tools.filebuilder;

    exports telekit.desktop.tools.ipcalc;
    opens telekit.desktop.tools.ipcalc;

    exports telekit.desktop.tools.passgen;
    opens telekit.desktop.tools.passgen;

    exports telekit.desktop.tools.seqgen;
    opens telekit.desktop.tools.seqgen;

    exports telekit.desktop.service;
}