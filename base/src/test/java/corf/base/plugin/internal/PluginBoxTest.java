package corf.base.plugin.internal;

import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import corf.base.OrdinaryTest;
import corf.base.plugin.*;

import java.util.List;

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
        public @Nullable Image getIcon() { return null; }

        @Override
        public List<? extends DependencyModule> getModules() { return null; }

        @Override
        public @Nullable ArtifactRepository getRepository() { return null; }

        @Override
        public void start() { }

        @Override
        public void stop() { }
    }
}
