package telekit.base.util;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.trim;

public final class Mappers {

    public static XmlMapper createXmlMapper() {
        XMLInputFactory inputFactory = new WstxInputFactory();
        inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, 32000);
        XMLOutputFactory outputFactory = new WstxOutputFactory();

        return XmlMapper.builder(new XmlFactory(inputFactory, outputFactory))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(new JakartaXmlBindAnnotationModule())
                .addModule(new StringTrimModule())
                .build();
    }

    public static YAMLMapper createYamlMapper() {
        YAMLFactory yamlFactory = new YAMLFactory()
                .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
                .disable(YAMLGenerator.Feature.SPLIT_LINES);

        return YAMLMapper.builder(yamlFactory)
                // if we don't serialize null fields it also mean that while deserialization
                // mapper will ignore missing properties
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
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
