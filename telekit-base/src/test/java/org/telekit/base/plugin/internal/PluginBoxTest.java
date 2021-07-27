package org.telekit.base.plugin.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.OrdinaryTest;
import org.telekit.base.di.DependencyModule;
import org.telekit.base.i18n.BundleLoader;
import org.telekit.base.plugin.Includes;
import org.telekit.base.plugin.Metadata;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.Tool;
import org.telekit.base.service.ArtifactRepository;
import org.telekit.base.service.crypto.EncryptionService;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@OrdinaryTest
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class PluginBoxTest {

    @Test
    @DisplayName("verify that plugin provides extensions of specified type")
    public void providesExtensionsOfType_SupportedExtensionType_Returned() {
        PluginBox pluginBox = new PluginBox(new ExamplePlugin(), PluginState.LOADED);

        assertThat(pluginBox.providesExtensionsOfType(Tool.class))
                .isTrue();
    }

    @Test
    @DisplayName("verify that plugin provides correct extension implementation")
    public void providesExtensionImpl_ValidImplementation_Success() {
        PluginBox pluginBox = new PluginBox(new ExamplePlugin(), PluginState.LOADED);

        assertThat(pluginBox.providesExtensionImpl(FooExtension.class))
                .isTrue();
        assertThat(pluginBox.providesExtensionImpl(BarExtension.class))
                .isFalse();
    }

    @Test
    @DisplayName("get extensions of specified type provided by plugin and assert correct result returned")
    public void getExtensionsOfType_SupportedExtensionType_Returned() {
        PluginBox pluginBox = new PluginBox(new ExamplePlugin(), PluginState.LOADED);

        assertThat(pluginBox.getExtensionsOfType(Tool.class))
                .containsOnly(FooExtension.class);
        assertThat(pluginBox.getExtensionsOfType(Tool.class))
                .hasSize(1);
    }

    @Includes(FooExtension.class)
    public static class ExamplePlugin implements Plugin {

        @Override
        public Metadata getMetadata() { return null; }

        @Override
        public List<? extends DependencyModule> getModules() { return null; }

        @Override
        public BundleLoader getBundleLoader() { return null; }

        @Override
        public Collection<String> getStylesheets() { return null; }

        @Override
        public ArtifactRepository getRepository() { return null; }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void updateEncryptedData(EncryptionService oldEncryptor, EncryptionService newEncryptor) {}

        @Override
        public boolean providesDocs() { return false; }

        @Override
        public void openDocs(Locale locale) {}
    }
}