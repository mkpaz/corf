package corf.desktop.tools.filebuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import corf.base.db.FileBasedRepository;
import corf.base.exception.AppException;
import corf.base.io.FileSystemUtils;
import corf.base.io.JacksonYamlSerializer;
import corf.base.io.Serializer;
import corf.desktop.i18n.DM;

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

import static corf.base.Env.CONFIG_DIR;
import static corf.base.i18n.I18n.t;
import static corf.base.io.FileSystemUtils.backupFile;
import static corf.base.io.FileSystemUtils.copyFile;

@Singleton
public final class TemplateRepository extends FileBasedRepository<Template, UUID> {

    public static final Path DATA_FILE_PATH = CONFIG_DIR.resolve("file-builder.tpl.yaml");
    private final Serializer<Collection<Template>> yamlSerializer;

    @Inject
    public TemplateRepository(YAMLMapper yamlMapper) {
        this.yamlSerializer = new JacksonYamlSerializer<>(yamlMapper, new TypeReference<>() { });
    }

    public Set<String> getNames() {
        return getAll().stream()
                .map(Template::getName)
                .collect(Collectors.toSet());
    }

    public void loadFromDisk() {
        // if database file doesn't exist we just start with empty repository
        if (!Files.exists(DATA_FILE_PATH)) { return; }

        try (InputStream inputStream = Files.newInputStream(DATA_FILE_PATH)) {
            if (count() > 0) { clear(); }
            load(inputStream, yamlSerializer);
        } catch (IOException e) {
            throw new AppException(t(DM.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public void saveToDisk() {
        Path backup = backupFile(DATA_FILE_PATH);
        try (OutputStream outputStream = Files.newOutputStream(DATA_FILE_PATH)) {
            yamlSerializer.serialize(outputStream, getAll());
        } catch (IOException e) {
            if (backup != null) {
                copyFile(backup, DATA_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
            throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        } finally {
            if (backup != null) {
                try {
                    FileSystemUtils.deleteFile(backup);
                } catch (Throwable ignored) { /* ignore */ }
            }
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
            throw new AppException(t(DM.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE), e);
        }
    }

    public void exportToFile(Collection<Template> templates, File outputFile) {
        try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            yamlSerializer.serialize(outputStream, templates);
        } catch (IOException e) {
            throw new AppException(t(DM.MGG_UNABLE_TO_SAVE_DATA_TO_FILE), e);
        }
    }
}
