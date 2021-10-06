import telekit.base.plugin.Plugin;

module telekit.base {

    uses Plugin;

    // modularized dependencies
    requires transitive java.desktop;
    requires transitive java.inject;
    requires transitive java.sql;
    requires transitive java.xml;
    requires transitive java.prefs;

    requires transitive javafx.controls;
    requires transitive javafx.fxml;

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
    requires org.jsoup;
    requires com.j2html;
    requires com.github.mustachejava;
    requires io.netty.all;
    requires org.yaml.snakeyaml;

    // dependencies without Automatic-Module-Name
    requires expectit.core;
    requires commons.dbutils;
    requires commons.csv;
    requires sshj;

    // exports
    exports telekit.base;
    exports telekit.base.desktop;
    exports telekit.base.desktop.mvvm;
    exports telekit.base.desktop.routing;
    exports telekit.base.di;
    exports telekit.base.domain;
    exports telekit.base.domain.event;
    exports telekit.base.domain.exception;
    exports telekit.base.domain.security;
    exports telekit.base.event;
    exports telekit.base.i18n;
    exports telekit.base.net;
    exports telekit.base.net.connection;
    exports telekit.base.plugin;
    exports telekit.base.plugin.internal to telekit.desktop;
    exports telekit.base.preferences;
    exports telekit.base.preferences.internal to telekit.desktop, com.fasterxml.jackson.databind;
    exports telekit.base.service;
    exports telekit.base.service.completion;
    exports telekit.base.service.crypto;
    exports telekit.base.service.jdbc;
    exports telekit.base.telecom;
    exports telekit.base.util;
    exports telekit.base.util.jdbc;

    opens telekit.base.domain to com.fasterxml.jackson.databind;
    opens telekit.base.domain.security to com.fasterxml.jackson.databind;
    opens telekit.base.net.connection to com.fasterxml.jackson.databind;
    opens telekit.base.preferences to com.fasterxml.jackson.databind;
    opens telekit.base.preferences.internal to com.fasterxml.jackson.databind;
}