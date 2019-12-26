module corf.desktop {

    ///////////////////////////////////////////////////////////////////////////
    // REQUIRES                                                              //
    ///////////////////////////////////////////////////////////////////////////

    requires corf.base;

    // dependencies with Automatic-Module-Name used by the module
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;
    requires me.gosimple.nbvcxz;

    requires org.slf4j.simple;
    requires org.slf4j.jdk.platform.logging;
    requires java.logging;
    requires jul.to.slf4j;
    requires org.apache.commons.logging;

    // dependencies without Automatic-Module-Name used by the module
    requires datafaker;

    // only to log JVM options
    requires java.management;

    ///////////////////////////////////////////////////////////////////////////
    // EXPORTS                                                               //
    ///////////////////////////////////////////////////////////////////////////

    exports corf.desktop;
    opens corf.desktop;
    exports corf.desktop.i18n;
    opens corf.desktop.i18n;
    exports corf.desktop.layout;
    opens corf.desktop.layout;
    exports corf.desktop.layout.preferences;
    opens corf.desktop.layout.preferences;
    exports corf.desktop.startup;
    opens corf.desktop.startup;
    exports corf.desktop.service;
    opens corf.desktop.service;

    // tools
    exports corf.desktop.tools.common;
    opens corf.desktop.tools.common;
    exports corf.desktop.tools.common.ui;
    opens corf.desktop.tools.common.ui;
    exports corf.desktop.tools.base64;
    opens corf.desktop.tools.base64;
    exports corf.desktop.tools.filebuilder;
    opens corf.desktop.tools.filebuilder;
    exports corf.desktop.tools.httpsender;
    opens corf.desktop.tools.httpsender;
    exports corf.desktop.tools.ipcalc;
    opens corf.desktop.tools.ipcalc;
    exports corf.desktop.tools.passgen;
    opens corf.desktop.tools.passgen;
    exports corf.desktop.tools.seqgen;
    opens corf.desktop.tools.seqgen;

    // assets
    opens corf.desktop.assets.dict;
    opens corf.desktop.assets.icons;
    opens corf.desktop.assets.icons.tools;
    opens corf.desktop.assets.styles;
}
