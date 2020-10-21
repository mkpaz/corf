package org.telekit.ui.tools.api_client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.Environment;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.Messages;
import org.telekit.base.service.FileBasedRepository;
import org.telekit.base.service.JacksonYamlSerializer;
import org.telekit.base.service.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.telekit.base.i18n.BaseMessageKeys.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE;
import static org.telekit.base.i18n.BaseMessageKeys.MGG_UNABLE_TO_SAVE_DATA_TO_FILE;

public class TemplateRepository extends FileBasedRepository<Template, UUID> {

    public static final Path DATA_FILE_PATH = Environment.DATA_DIR.resolve("api-client.templates.yaml");
    private final Serializer<Collection<Template>> yamlSerializer;

    public TemplateRepository(YAMLMapper yamlMapper) {
        this.yamlSerializer = new JacksonYamlSerializer<>(yamlMapper, new TypeReference<>() {});
    }

    public Set<String> getNames() {
        return getAll().stream()
                .map(Template::getName)
                .collect(Collectors.toSet());
    }

    public void reloadAll() {
        // if database file doesn't exist we just start with empty repository
        if (!Files.exists(DATA_FILE_PATH)) return;

        try (InputStream inputStream = Files.newInputStream(DATA_FILE_PATH)) {
            if (size() > 0) clear();
            load(inputStream, yamlSerializer);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public void saveAll() {
        try (OutputStream outputStream = Files.newOutputStream(DATA_FILE_PATH)) {
            yamlSerializer.serialize(outputStream, getAll());
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }

    public void importFromFile(@NotNull File inputFile) {
        try (InputStream inputStream = Files.newInputStream(inputFile.toPath())) {
            for (Template template : yamlSerializer.deserialize(inputStream)) {
                if (contains(template)) {
                    update(template);
                } else {
                    add(template);
                }
            }
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public void exportToFile(@NotNull Collection<Template> templates, @NotNull File outputFile) {
        try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            yamlSerializer.serialize(outputStream, templates);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }
}
