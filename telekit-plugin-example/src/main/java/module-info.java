import org.telekit.base.plugin.Plugin;

module telekit.example {

    requires telekit.base;
    requires telekit.controls;
    requires org.jetbrains.annotations;

    exports org.telekit.example;
    opens org.telekit.example;

    exports org.telekit.example.service;
    opens org.telekit.example.service;

    exports org.telekit.example.tools;
    opens org.telekit.example.tools;

    provides Plugin with org.telekit.example.ExamplePlugin;

    // demo package
    exports org.telekit.example.demo;
    opens org.telekit.example.demo;
}