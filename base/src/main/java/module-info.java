import corf.base.plugin.Plugin;

module corf.base {

    uses Plugin;

    ///////////////////////////////////////////////////////////////////////////
    // REQUIRES                                                              //
    ///////////////////////////////////////////////////////////////////////////

    // modularized dependencies
    requires transitive java.desktop;
    requires transitive java.sql;
    requires transitive java.xml;
    requires transitive java.prefs;

    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.swing;

    requires transitive atlantafx.base;
    requires transitive backbonefx;
    requires transitive org.kordamp.ikonli.core;
    requires transitive org.kordamp.ikonli.javafx;
    requires transitive org.kordamp.ikonli.material2;
    requires transitive eu.hansolo.medusa;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.dataformat.javaprop;
    requires transitive com.fasterxml.jackson.dataformat.xml;
    requires transitive com.fasterxml.jackson.module.jakarta.xmlbind;
    requires transitive com.fasterxml.jackson.dataformat.yaml;

    requires transitive com.ctc.wstx;
    requires transitive de.skuzzle.semantic;
    requires transitive inet.ipaddr;
    requires transitive org.hsqldb;
    requires transitive org.jetbrains.annotations;
    requires transitive org.bouncycastle.pkix;
    requires transitive org.bouncycastle.provider;
    requires transitive org.slf4j;
    requires transitive org.snmp4j;

    // dependencies with Automatic-Module-Name
    requires org.apache.commons.codec;
    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.commons.net;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.httpcomponents.core5.httpcore5.h2;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.fontbox;
    requires org.apache.pdfbox;
    requires org.flywaydb.core;
    requires org.fxmisc.flowless;
    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    requires org.jsoup;
    requires com.j2html;
    requires com.github.mustachejava;
    requires com.hierynomus.sshj;
    requires me.gosimple.nbvcxz;
    requires org.yaml.snakeyaml;

    // dependencies without Automatic-Module-Name
    requires commons.csv;
    requires commons.dbutils;
    requires datafaker;
    requires dflib;
    requires dflib.csv;
    requires expectit.core;
    requires kaitai.struct.runtime;
    requires reactfx;
    requires wellbehavedfx;

    ///////////////////////////////////////////////////////////////////////////
    // EXPORTS                                                               //
    ///////////////////////////////////////////////////////////////////////////

    // export public API
    exports corf.base;
    exports corf.base.collection;
    exports corf.base.common;
    exports corf.base.db;
    exports corf.base.db.jdbc;
    exports corf.base.desktop;
    exports corf.base.desktop.controls;
    exports corf.base.exception;
    exports corf.base.event;
    exports corf.base.i18n;
    exports corf.base.io;
    exports corf.base.net;
    exports corf.base.plugin;
    exports corf.base.preferences;
    exports corf.base.security;
    exports corf.base.text;

    // internal implementation should be visible to core app only
    exports corf.base.plugin.internal to corf.desktop;
    exports corf.base.preferences.internal to corf.desktop;

    // open all packages that contain serializable data classes to Jackson
    opens corf.base.common to com.fasterxml.jackson.databind;
    opens corf.base.db to com.fasterxml.jackson.databind;
    opens corf.base.net to com.fasterxml.jackson.databind;
    opens corf.base.security to com.fasterxml.jackson.databind;
    opens corf.base.preferences to com.fasterxml.jackson.databind;
    opens corf.base.preferences.internal to com.fasterxml.jackson.databind;
    opens corf.base.text to com.fasterxml.jackson.databind;

    // open i18n resources to any other module
    opens corf.base.i18n;

    // open assets to any other module
    opens corf.base.assets;
    opens corf.base.assets.fonts.Inter;
    opens corf.base.assets.fonts.FiraMono;
    opens corf.base.assets.fonts.Roboto;
}
