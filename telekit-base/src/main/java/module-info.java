import org.telekit.base.plugin.Plugin;

module telekit.base {

    uses Plugin;

    // modularized dependencies
    requires transitive telekit.controls;

    requires transitive java.desktop;
    requires transitive java.inject;
    requires transitive java.sql;
    requires transitive java.xml;

    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.swing;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.dataformat.csv;
    requires transitive com.fasterxml.jackson.dataformat.javaprop;
    requires transitive com.fasterxml.jackson.dataformat.xml;
    requires transitive com.fasterxml.jackson.dataformat.yaml;

    requires transitive com.ctc.wstx;
    requires transitive de.skuzzle.semantic;
    requires transitive inet.ipaddr;

    // dependencies with Automatic-Module-Name
    requires org.apache.commons.codec;
    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.apache.commons.net;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpclient.fluent;
    requires org.jetbrains.annotations;
    requires org.jsoup;
    requires org.yaml.snakeyaml;

    // dependencies without Auto-Module-Name
    requires hsqldb;
    requires commons.dbutils;
    requires j2html;

    // exports
    exports org.telekit.base;
    exports org.telekit.base.domain;
    exports org.telekit.base.feather;
    exports org.telekit.base.i18n;
    exports org.telekit.base.preferences;
    exports org.telekit.base.plugin;
    exports org.telekit.base.plugin.internal to telekit.ui;
    exports org.telekit.base.service;
    exports org.telekit.base.service.impl;
    exports org.telekit.base.telecom.net;
    exports org.telekit.base.telecom.ss7;
    exports org.telekit.base.ui;
    exports org.telekit.base.util;
}