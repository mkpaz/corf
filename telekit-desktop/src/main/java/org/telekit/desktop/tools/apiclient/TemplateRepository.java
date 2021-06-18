package org.telekit.desktop.tools.apiclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.i18n.I18n;
import org.telekit.base.service.FileBasedRepository;
import org.telekit.base.service.Serializer;
import org.telekit.base.service.impl.JacksonYamlSerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.telekit.base.Env.CONFIG_DIR;
import static org.telekit.base.i18n.BaseMessages.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE;
import static org.telekit.base.i18n.BaseMessages.MGG_UNABLE_TO_SAVE_DATA_TO_FILE;
import static org.telekit.base.util.CommonUtils.hush;
import static org.telekit.base.util.FileUtils.*;

public class TemplateRepository extends FileBasedRepository<Template, UUID> {

    public static final Path DATA_FILE_PATH = CONFIG_DIR.resolve("api-client.templates.yaml");
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
        if (!Files.exists(DATA_FILE_PATH)) { return; }

        try (InputStream inputStream = Files.newInputStream(DATA_FILE_PATH)) {
            if (size() > 0) { clear(); }
            load(inputStream, yamlSerializer);
        } catch (IOException e) {
            throw new TelekitException(I18n.t(MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public void saveAll() {
        Path backup = backupFile(DATA_FILE_PATH);
        try (OutputStream outputStream = Files.newOutputStream(DATA_FILE_PATH)) {
            yamlSerializer.serialize(outputStream, getAll());
        } catch (IOException e) {
            if (backup != null) {
                copyFile(backup, DATA_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
            throw new TelekitException(I18n.t(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        } finally {
            if (backup != null) { hush(() -> deleteFile(backup)); }
        }
    }

    public void importFromFile(File inputFile) {
        try (InputStream inputStream = Files.newInputStream(inputFile.toPath())) {
            for (Template template : yamlSerializer.deserialize(inputStream)) {
                if (contains(template)) {
                    update(template);
                } else {
                    add(template);
                }
            }
        } catch (IOException e) {
            throw new TelekitException(I18n.t(MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public void exportToFile(Collection<Template> templates, File outputFile) {
        try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            yamlSerializer.serialize(outputStream, templates);
        } catch (IOException e) {
            throw new TelekitException(I18n.t(MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }
}
