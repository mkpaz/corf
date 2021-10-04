import telekit.base.plugin.Plugin;

module telekit.example {

    requires telekit.base;
    requires telekit.controls;
    requires org.jetbrains.annotations;
    requires org.apache.commons.lang3;

    exports telekit.example;
    opens telekit.example;

    exports telekit.example.service;
    opens telekit.example.service;

    exports telekit.example.tools;
    opens telekit.example.tools;

    provides Plugin with telekit.example.ExamplePlugin;

    exports telekit.example.demo;
    opens telekit.example.demo;

    exports telekit.example.i18n;
    opens telekit.example.i18n;
}