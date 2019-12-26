package corf.example;

import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;
import corf.base.common.ClasspathResource;
import corf.base.exception.AppException;
import corf.base.plugin.*;
import corf.example.i18n.EM;
import corf.example.tools.HelloTool;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.apache.commons.lang3.ClassUtils.getCanonicalName;

@Includes(HelloTool.class)
public class ExamplePlugin implements Plugin {

    public static final ClasspathResource MODULE_PATH = ClasspathResource.of("/corf/example", ExamplePlugin.class);
    public static final String METADATA_FILE = "plugin.properties";
    public static final Image PLUGIN_ICON = new Image(Objects.requireNonNull(
            MODULE_PATH.concat("assets/icon.png").getResourceAsStream()
    ));

    private final Metadata metadata = new Metadata();

    public ExamplePlugin() {
        try (var reader = new InputStreamReader(
                Objects.requireNonNull(MODULE_PATH.concat(METADATA_FILE).getResourceAsStream()),
                StandardCharsets.UTF_8)
        ) {
            var properties = new Properties();
            properties.load(reader);

            metadata.setName(properties.getProperty(METADATA_NAME));
            metadata.setAuthor(properties.getProperty(METADATA_AUTHOR));
            metadata.setVersion(properties.getProperty(METADATA_VERSION));
            metadata.setDescription(properties.getProperty(METADATA_DESCRIPTION));
            metadata.setHomePage(properties.getProperty(METADATA_HOMEPAGE));
            metadata.setPlatformVersion(properties.getProperty(METADATA_PLATFORM_VERSION));
        } catch (IOException e) {
            throw new AppException(EM.MSG_GENERIC_IO_ERROR, e);
        }
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public @Nullable Image getIcon() {
        return PLUGIN_ICON;
    }

    @Override
    public List<DependencyModule> getModules() {
        return Collections.singletonList(new ExampleDependencyModule());
    }

    @Override
    public @Nullable ArtifactRepository getRepository() {
        return null;
    }

    @Override
    public void start() {
        System.out.println(getCanonicalName(ExamplePlugin.class) + " start() method called.");
    }

    @Override
    public void stop() {
        System.out.println(getCanonicalName(ExamplePlugin.class) + " stop() method called.");
    }
}
