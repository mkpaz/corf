module telekit.base {

    // modularized dependencies
    requires transitive telekit.controls;

    requires transitive java.desktop;
    requires transitive java.inject;
    requires transitive java.sql;
    requires transitive java.xml;

    requires transitive javafx.controls;
    requires transitive javafx.fxml;

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

    // not modularized dependencies
    requires org.apache.commons.codec;
    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpclient.fluent;
    requires org.jetbrains.annotations;
    requires org.jsoup;
    requires org.yaml.snakeyaml;

    // dependencies without Auto-Module-Name
    requires hsqldb;
    requires commons.dbutils;
    requires commons.net;
    requires j2html;

    // exports
    exports org.telekit.base;
    exports org.telekit.base.domain;
    exports org.telekit.base.feather;
    exports org.telekit.base.fx;
    exports org.telekit.base.internal to telekit.ui, com.fasterxml.jackson.databind;
    exports org.telekit.base.plugin;
    exports org.telekit.base.service;
    exports org.telekit.base.util;
    exports org.telekit.base.util.net;
    exports org.telekit.base.util.telecom;
    exports org.telekit.base.util.tramsliterate;
}