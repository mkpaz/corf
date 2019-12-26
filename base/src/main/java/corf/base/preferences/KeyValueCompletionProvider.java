package corf.base.preferences;

import org.apache.commons.lang3.StringUtils;
import corf.base.common.KeyValue;
import corf.base.i18n.M;
import corf.base.io.FileSystemUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static corf.base.i18n.I18n.t;

/**
 * This completion provider parses property file and represents
 * its content as a {@link KeyValue} collection.
 */
public class KeyValueCompletionProvider implements CompletionProvider<KeyValue<String, String>> {

    private final String key;
    private final Path path;

    public KeyValueCompletionProvider(String key, Path filePath) {
        this.key = Objects.requireNonNull(key, "key");
        this.path = Objects.requireNonNull(filePath, "filePath");
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Collection<KeyValue<String, String>> matches(String s) {
        return find(key -> key.matches(s));
    }

    @SuppressWarnings("StringSplitter")
    public Collection<KeyValue<String, String>> find(Predicate<String> predicate) {
        List<KeyValue<String, String>> result = new ArrayList<>();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return Collections.emptyList();
        }

        try (var is = new FileInputStream(path.toFile());
             var reader = new BufferedReader(new InputStreamReader(is, UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.charAt(0) == '#') { continue; }
                String[] chunks = line.split("=");
                if (chunks.length == 0) { continue; }

                String key = chunks[0];
                String value = chunks.length > 1 ? chunks[1] : "";

                if (predicate.test(key)) {
                    result.add(new KeyValue<>(key.trim(), value.trim()));
                }
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(result);
    }

    ///////////////////////////////////////////////////////////////////////////

    /** Creates (writes) a template file that can be used by this provider and return its path. */
    public static Path createTemplateFile(Path dir, String key) throws IOException {
        boolean valid = dir != null
                && StringUtils.isNotBlank(key)
                && FileSystemUtils.dirExists(dir)
                && !FileSystemUtils.fileExists(resolve(dir, key));

        if (!valid) {
            throw new IOException(t(M.MSG_GENERIC_IO_ERROR));
        }

        var filePath = resolve(dir, key);
        var head = """
                # This is an completion / suggestion file. It's used to provide hints when filling in
                # application forms. To use this feature the file name exactly must be same as target
                # parameter (field) name. All lines started from '#' will be ignored.
                # Example:
                # key1=foo
                # key2=bar
                """;

        Files.writeString(filePath, head);

        return filePath;
    }

    public static Path resolve(Path dir, String key) {
        return dir.resolve(key + ".properties");
    }
}
