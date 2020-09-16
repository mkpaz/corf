package org.telekit.example;

import org.telekit.base.plugin.DependencyModule;
import org.telekit.base.plugin.Metadata;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.Tool;
import org.telekit.base.util.DesktopUtils;
import org.telekit.example.service.ExampleDependencyModule;
import org.telekit.example.tools.ExampleTool;

import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ExamplePlugin implements Plugin {

    public static final String ASSETS_PATH = "/org/telekit/example/assets/";
    public static final String PLUGIN_PROPERTIES = "plugin.properties";
    public static final String SAMPLE_PROPERTIES = "sample.properties";

    private final Metadata metadata;

    public ExamplePlugin() throws Exception {
        metadata = new Metadata();

        Properties properties = new Properties();
        properties.load(new InputStreamReader(
                ExamplePlugin.class.getResourceAsStream(ASSETS_PATH + PLUGIN_PROPERTIES),
                StandardCharsets.UTF_8
        ));

        metadata.setName(properties.getProperty(METADATA_NAME));
        metadata.setAuthor(properties.getProperty(METADATA_AUTHOR));
        metadata.setVersion(properties.getProperty(METADATA_VERSION));
        metadata.setDescription(properties.getProperty(METADATA_DESCRIPTION));
        metadata.setHomePage(properties.getProperty(METADATA_HOMEPAGE));
        metadata.setRequiredVersion(properties.getProperty(METADATA_REQUIRES_VERSION));
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public List<DependencyModule> getModules() {
        return Collections.singletonList(new ExampleDependencyModule());
    }

    @Override
    public List<Tool> getTools() {
        return List.of(new ExampleTool());
    }

    @Override
    public void onLoad() {
        // not yet implemented
    }

    @Override
    public void openDocs() {
        DesktopUtils.browseQuietly(URI.create("https://example.org"));
        // or delegate this action to another method via EventBus
    }

    @Override
    public boolean providesDocs() {
        return true;
    }
}
