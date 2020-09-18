package org.telekit.base;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.*;

public class Messages extends ResourceBundle {

    private Map<String, String> resources = new HashMap<>();

    private Messages() {}

    private static class InstanceHolder {
        private static final Messages INSTANCE = new Messages();
    }

    public static Messages getInstance() {
        return Messages.InstanceHolder.INSTANCE;
    }

    ///////////////////////////////////////////////////////////////////////////

    public void loadFromBundles(List<ResourceBundle> bundles) {
        bundles.forEach(bundle -> {
            ArrayList<String> keysList = Collections.list(bundle.getKeys());
            keysList.forEach(key -> resources.put(key, bundle.getString(key)));
        });
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
}