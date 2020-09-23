package org.telekit.ui;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.telekit.base.Provides;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.plugin.DependencyModule;
import org.telekit.ui.service.PluginManager;

import javax.inject.Singleton;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.nio.file.Files;

import static org.apache.commons.lang3.StringUtils.trim;

public class MainDependencyModule implements DependencyModule {

    private final XmlMapper mapper;
    private final PluginManager pluginManager;

    public MainDependencyModule(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.mapper = createDefaultMapper();
    }

    @Provides
    @Singleton
    public PluginManager pluginManager() {
        return pluginManager;
    }

    @Provides
    @Singleton
    public ApplicationPreferences applicationPreferences() {
        ApplicationPreferences preferences;

        if (Files.exists(ApplicationPreferences.CONFIG_PATH)) {
            preferences = ApplicationPreferences.load(mapper, ApplicationPreferences.CONFIG_PATH);
        } else {
            preferences = new ApplicationPreferences();
            ApplicationPreferences.store(preferences, mapper, ApplicationPreferences.CONFIG_PATH);
        }

        return preferences;
    }

    @Provides
    @Singleton
    public XmlMapper xmlMapper() {
        return createDefaultMapper();
    }

    public static XmlMapper createDefaultMapper() {
        XMLInputFactory inputFactory = new WstxInputFactory();
        inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, 32000);
        XMLOutputFactory outputFactory = new WstxOutputFactory();

        return XmlMapper.builder(new XmlFactory(inputFactory, outputFactory))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(new StringTrimModule())
                .build();
    }

    public static class StringTrimModule extends SimpleModule {

        public StringTrimModule() {
            addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {
                @Override
                public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
                    return trim(jsonParser.getValueAsString());
                }
            });
        }
    }
}
