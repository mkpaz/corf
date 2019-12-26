package corf.base.i18n;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class I18n extends ResourceBundle {

    private final Map<String, BundleLoader> loaders = new HashMap<>();
    private Map<String, String> messages = new HashMap<>();

    public void register(BundleLoader loader) {
        loaders.putIfAbsent(loader.id(), loader);
    }

    public void unregister(String id) {
        loaders.remove(id);
    }

    public void reload() {
        messages = loaders.values().stream()
                .flatMap(loader -> loader.load(Locale.getDefault()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public String translate(String text) {
        return messages.getOrDefault(text, text);
    }

    public String translate(String text, Object... args) {
        return MessageFormat.format(translate(text), args);
    }

    public void print(PrintStream out) {
        messages.forEach((key, value) -> out.println(key + "=" + value));
    }

    @Override
    protected String handleGetObject(@NotNull String key) {
        return translate(key);
    }

    @Override
    public @NotNull Enumeration<String> getKeys() {
        return Collections.enumeration(messages.keySet());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Singleton                                                             //
    ///////////////////////////////////////////////////////////////////////////

    private I18n() { }

    private static class InstanceHolder {

        private static final I18n INSTANCE = new I18n();
    }

    public static I18n getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static String t(String text) {
        return getInstance().translate(text);
    }

    public static String t(String text, Object... args) {
        return getInstance().translate(text, args);
    }

    public static String concat(String... keys) {
        return concat(" ".toCharArray(), keys);
    }

    public static String concat(char[] separator, String... keys) {
        return Arrays.stream(keys)
                .map(key -> getInstance().getString(key))
                .collect(Collectors.joining(new String(separator)));
    }
}
