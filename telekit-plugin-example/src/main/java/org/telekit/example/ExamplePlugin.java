package org.telekit.example;

import org.telekit.base.di.DependencyModule;
import org.telekit.base.i18n.BundleLoader;
import org.telekit.base.plugin.Includes;
import org.telekit.base.plugin.Metadata;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.service.ArtifactRepository;
import org.telekit.base.service.EncryptionService;
import org.telekit.base.util.DesktopUtils;
import org.telekit.example.i18n.ExampleMessages;
import org.telekit.example.service.ExampleDependencyModule;
import org.telekit.example.tools.HelloTool;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.apache.commons.lang3.ClassUtils.getCanonicalName;
import static org.telekit.base.Env.DOCS_INDEX_FILE_NAME;
import static org.telekit.base.Env.getPluginDocsDir;

@Includes(HelloTool.class)
public class ExamplePlugin implements Plugin {

    public static final String ASSETS_PATH = "/org/telekit/example/assets";
    public static final String PLUGIN_PROPERTIES_FILE_NAME = "/org/telekit/example/plugin.properties";
    public static final String SAMPLE_PROPERTIES_FILE_NAME = "sample.properties";

    private final Metadata metadata;

    public ExamplePlugin() throws Exception {
        metadata = new Metadata();

        Properties properties = new Properties();
        InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(ExamplePlugin.class.getResourceAsStream(PLUGIN_PROPERTIES_FILE_NAME)),
                StandardCharsets.UTF_8
        );
        properties.load(reader);

        metadata.setName(properties.getProperty(METADATA_NAME));
        metadata.setAuthor(properties.getProperty(METADATA_AUTHOR));
        metadata.setVersion(properties.getProperty(METADATA_VERSION));
        metadata.setDescription(properties.getProperty(METADATA_DESCRIPTION));
        metadata.setHomePage(properties.getProperty(METADATA_HOMEPAGE));
        metadata.setPlatformVersion(properties.getProperty(METADATA_PLATFORM_VERSION));
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
    public BundleLoader getBundleLoader() {
        return BundleLoader.of(ExampleMessages.class);
    }

    @Override
    public Collection<String> getStylesheets() { return Collections.emptyList(); }

    @Override
    public ArtifactRepository getRepository() { return null; }

    @Override
    public void start() {
        System.out.println(getCanonicalName(ExamplePlugin.class) + " start() method called.");
    }

    @Override
    public void stop() {
        System.out.println(getCanonicalName(ExamplePlugin.class) + " stop() method called.");
    }

    @Override
    public void updateEncryptedData(EncryptionService oldEncryptor, EncryptionService newEncryptor) {}

    @Override
    public boolean providesDocs() {
        return true;
    }

    @Override
    public void openDocs(Locale locale) {
        Path docsDir = getPluginDocsDir(ExamplePlugin.class);
        Path localizedFilePath = docsDir.resolve(
                locale != null ?
                        DOCS_INDEX_FILE_NAME + "_" + locale.getLanguage() + ".txt" :
                        DOCS_INDEX_FILE_NAME + ".txt"
        );

        if (Files.exists(localizedFilePath)) {
            DesktopUtils.openQuietly(localizedFilePath.toFile());
        } else {
            DesktopUtils.openQuietly(docsDir.resolve(DOCS_INDEX_FILE_NAME + ".txt").toFile());
        }
    }
}
