package org.telekit.base.plugin.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.telekit.base.OrdinaryTest;
import org.telekit.base.plugin.Extension;
import org.telekit.base.plugin.Includes;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.Tool;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@OrdinaryTest
public class PluginLoaderTest {

    @Test
    @DisplayName("verify that loader ignores abstract classes and interfaces")
    public void resolveExtensionTypes_AbstractClassOrInterface_Ignored() {

        @Includes({AbstractExtension.class, InterfaceExtension.class})
        abstract class TestPlugin implements Plugin {}

        Map<Class<? extends Extension>, Set<Class<? extends Extension>>> extensionsMap =
                PluginLoader.resolveExtensionTypes(TestPlugin.class);
        assertThat(extensionsMap).isEmpty();
    }

    @Test
    @DisplayName("verify that loader ignores unknown types of extensions")
    public void resolveExtensionTypes_UnknownExtensionType_Ignored() {

        @Includes({UnknownExtension.class})
        abstract class TestPlugin implements Plugin {}

        Map<Class<? extends Extension>, Set<Class<? extends Extension>>> extensionsMap =
                PluginLoader.resolveExtensionTypes(TestPlugin.class);
        assertThat(extensionsMap).isEmpty();
    }

    @Test
    @DisplayName("verify that loader resolves valid extension types")
    public void resolveExtensionTypes_ValidExtensions_Resolved() {

        @Includes({FooExtension.class, BarExtension.class})
        abstract class TestPlugin implements Plugin {}

        Map<Class<? extends Extension>, Set<Class<? extends Extension>>> extensionsMap =
                PluginLoader.resolveExtensionTypes(TestPlugin.class);

        assertThat(extensionsMap).hasSize(1);
        assertThat(extensionsMap.get(Tool.class))
                .containsAll(Set.of(FooExtension.class, BarExtension.class));
    }

    @SuppressWarnings("rawtypes")
    public static abstract class AbstractExtension implements Tool {}

    @SuppressWarnings("rawtypes")
    public interface InterfaceExtension extends Tool {}

    public static class UnknownExtension implements Extension {}
}