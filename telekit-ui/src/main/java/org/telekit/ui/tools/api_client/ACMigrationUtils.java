package org.telekit.ui.tools.api_client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.telekit.base.ApplicationContext;
import org.telekit.base.Env;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.Messages;
import org.telekit.base.service.JacksonYamlSerializer;
import org.telekit.base.service.Serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.telekit.base.i18n.BaseMessageKeys.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE;
import static org.telekit.base.i18n.BaseMessageKeys.MGG_UNABLE_TO_SAVE_DATA_TO_FILE;

@Deprecated
public class ACMigrationUtils {

    private static final Path DATA_FILE_PATH_OLD = Env.DATA_DIR.resolve("api-client.templates.xml");

    public static void migrateXmlConfigToYaml(ApplicationContext context) {
        // database file already converted
        if (Files.exists(TemplateRepository.DATA_FILE_PATH)) return;
        // database file is empty
        if (!Files.exists(DATA_FILE_PATH_OLD)) return;

        XmlMapper xmlMapper = context.getBean(XmlMapper.class);
        YAMLMapper yamlMapper = context.getBean(YAMLMapper.class);

        Serializer<Collection<Template>> xmlSerializer = new XmlSerializer(xmlMapper);
        Serializer<Collection<Template>> yamlSerializer = new JacksonYamlSerializer<>(yamlMapper, new TypeReference<>() {});

        try (InputStream inputStream = Files.newInputStream(DATA_FILE_PATH_OLD);
             OutputStream outputStream = Files.newOutputStream(TemplateRepository.DATA_FILE_PATH)) {

            Collection<Template> data = xmlSerializer.deserialize(inputStream);
            yamlSerializer.serialize(outputStream, data);
            Files.delete(DATA_FILE_PATH_OLD);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static class XmlSerializer implements Serializer<Collection<Template>> {

        private final XmlMapper mapper;

        public XmlSerializer(XmlMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public void serialize(OutputStream outputStream, Collection<Template> templates) {
            try {
                mapper.writeValue(outputStream, new Wrapper(templates));
            } catch (Exception e) {
                throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
            }
        }

        @Override
        public Collection<Template> deserialize(InputStream inputStream) {
            List<Template> result = new ArrayList<>();

            try {
                Wrapper data = mapper.readValue(inputStream, Wrapper.class);
                if (data.getTemplates() != null && data.getTemplates().size() > 0) {
                    result.addAll(data.getTemplates());
                }
            } catch (Exception e) {
                throw new TelekitException(Messages.get(MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
            }

            return result;
        }
    }

    @Deprecated
    @JacksonXmlRootElement(localName = "templates")
    public static class Wrapper {

        @JacksonXmlProperty(localName = "template")
        @JacksonXmlElementWrapper(useWrapping = false)
        private Collection<Template> templates;

        public Wrapper() {}

        public Wrapper(Collection<Template> templates) {
            this.templates = templates;
        }

        public Collection<Template> getTemplates() {
            return templates;
        }

        public void setTemplates(List<Template> templates) {
            this.templates = templates;
        }
    }
}
