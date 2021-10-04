package telekit.base.service.completion;

import telekit.base.domain.KeyValue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class KeyValueCompletionProvider implements CompletionProvider<KeyValue<String, String>> {

    private final String key;
    private final Path filePath;

    public KeyValueCompletionProvider(String key, Path filePath) {
        this.key = Objects.requireNonNull(key);
        this.filePath = Objects.requireNonNull(filePath);
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Collection<KeyValue<String, String>> startsWith(String str) {
        return find(key -> key.startsWith(str));
    }

    @Override
    public Collection<KeyValue<String, String>> contains(String str) {
        return find(key -> key.contains(str));
    }

    @Override
    public Collection<KeyValue<String, String>> matches(String pattern) {
        return find(key -> key.matches(pattern));
    }

    public Collection<KeyValue<String, String>> find(Predicate<String> predicate) {
        List<KeyValue<String, String>> result = new ArrayList<>();
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) { return Collections.emptyList(); }

        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8))) {

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
        return result;
    }
}
