module org.jsoup {

    // excluding JSR305 as it's a build-time dependency
    requires java.xml;

    exports org.jsoup;
    exports org.jsoup.helper;
    exports org.jsoup.internal;
    exports org.jsoup.nodes;
    exports org.jsoup.parser;
    exports org.jsoup.safety;
    exports org.jsoup.select;
}