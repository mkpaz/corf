package org.telekit.ui.tools.import_file_builder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.Environment;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.Messages;
import org.telekit.base.service.Serializer;
import org.telekit.base.service.FileBasedRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.telekit.ui.main.MessageKeys.MGG_UNABLE_TO_PARSE_CONFIG;
import static org.telekit.ui.main.MessageKeys.MGG_UNABLE_TO_SAVE_CONFIG;

public class TemplateRepository extends FileBasedRepository<Template, UUID> {

    private static final Path DATA_FILE_PATH_XML = Environment.DATA_DIR.resolve("import-file-builder.templates.xml");
    private final TemplateRepository.XmlSerializer serializer;

    public TemplateRepository(XmlMapper mapper) {
        this.serializer = new TemplateRepository.XmlSerializer(mapper);
    }

    public Set<String> getNames() {
        return getAll().stream()
                .map(Template::getName)
                .collect(Collectors.toSet());
    }

    public void reloadAll() {
        // if database file doesn't exist we just start with empty repository
        if (!Files.exists(DATA_FILE_PATH_XML)) return;

        try (InputStream inputStream = Files.newInputStream(DATA_FILE_PATH_XML)) {
            if (size() > 0) clear();
            load(inputStream, serializer);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_PARSE_CONFIG), e);
        }
    }

    public void saveAll() {
        try (OutputStream outputStream = Files.newOutputStream(DATA_FILE_PATH_XML)) {
            serializer.serialize(outputStream, getAll());
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_CONFIG), e);
        }
    }

    public void importFromFile(@NotNull File inputFile) {
        try (InputStream inputStream = Files.newInputStream(inputFile.toPath())) {
            for (Template template : serializer.deserialize(inputStream)) {
                if (contains(template)) {
                    update(template);
                } else {
                    add(template);
                }
            }
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_PARSE_CONFIG), e);
        }
    }

    public void exportToFile(@NotNull Collection<Template> templates, @NotNull File outputFile) {
        try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            serializer.serialize(outputStream, templates);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_CONFIG), e);
        }
    }

    public static class XmlSerializer implements Serializer<Collection<Template>> {

        private final XmlMapper mapper;

        public XmlSerializer(XmlMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public void serialize(OutputStream outputStream, Collection<Template> templates) {
            try {
                mapper.writeValue(outputStream, new TemplateRepository.Wrapper(templates));
            } catch (Exception e) {
                throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_CONFIG), e);
            }
        }

        @Override
        public Collection<Template> deserialize(InputStream inputStream) {
            List<Template> result = new ArrayList<>();

            try {
                TemplateRepository.Wrapper data = mapper.readValue(inputStream, TemplateRepository.Wrapper.class);
                if (data.getTemplates() != null && data.getTemplates().size() > 0) {
                    result.addAll(data.getTemplates());
                }
            } catch (Exception e) {
                throw new TelekitException(Messages.get(MGG_UNABLE_TO_PARSE_CONFIG), e);
            }

            return result;
        }
    }

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
