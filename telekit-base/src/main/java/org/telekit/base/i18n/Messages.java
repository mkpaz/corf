package org.telekit.base.i18n;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.*;

public class Messages extends ResourceBundle {

    private final Map<String, String> resources = new HashMap<>();
    private final Set<String> loadedID = new HashSet<>();

    private Messages() {}

    private static class InstanceHolder {
        private static final Messages INSTANCE = new Messages();
    }

    public static Messages getInstance() {
        return Messages.InstanceHolder.INSTANCE;
    }

    ///////////////////////////////////////////////////////////////////////////

    public void load(@NotNull ResourceBundle bundle, @NotNull String id) {
        if (loadedID.contains(id)) return;
        ArrayList<String> keysList = Collections.list(bundle.getKeys());
        keysList.forEach(key -> resources.put(key, bundle.getString(key)));
        loadedID.add(id);
    }

    @Override
    public Object handleGetObject(@NotNull String key) {
        return resources.get(key);
    }

    @Override
    public @NotNull Enumeration<String> getKeys() {
        return Collections.enumeration(resources.keySet());
    }

    public static String get(String key) {
        return getInstance().getString(key);
    }

    public static String get(String key, Object... args) {
        String pattern = getInstance().getString(key);
        return MessageFormat.format(pattern, args);
    }

    public void print(PrintStream out) {
        resources.forEach((key, value) -> out.println(key + "=" + value));
    }
}