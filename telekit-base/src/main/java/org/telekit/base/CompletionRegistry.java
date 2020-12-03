package org.telekit.base;

import org.telekit.base.service.CompletionProvider;

import java.util.*;

import static org.telekit.base.service.CompletionProvider.isValidKey;

/**
 * Global registry for completion providers.
 * Use DI to get the instance.
 */
public class CompletionRegistry {

    private final Map<String, CompletionProvider<?>> providers = new HashMap<>();

    public Collection<String> getSupportedKeys() {
        return new ArrayList<>(providers.keySet());
    }

    public boolean isSupported(String key) {
        if (key == null) return false;
        return providers.containsKey(key);
    }

    public Collection<CompletionProvider<?>> getProviders() {
        return new ArrayList<>(providers.values());
    }

    public Optional<CompletionProvider<?>> getProviderFor(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(providers.get(key));
    }

    public void registerProvider(CompletionProvider<?> provider) {
        if (isValidKey(provider.key())) {
            // duplicated keys aren't supported, provider must be unregistered
            providers.putIfAbsent(provider.key(), provider);
        }
    }

    public void unregisterProvider(String key) {
        if (isValidKey(key)) {
            providers.remove(key);
        }
    }
}
