package org.telekit.desktop.service;

import org.telekit.base.service.completion.CompletionProvider;
import org.telekit.base.service.completion.CompletionRegistry;

import java.util.*;

import static org.telekit.base.service.completion.CompletionProvider.isValidKey;

/**
 * Global registry for completion providers.
 * Use DI to get the instance.
 */
public class DefaultCompletionRegistry implements CompletionRegistry {

    private final Map<String, CompletionProvider<?>> providers = new HashMap<>();

    @Override
    public Collection<String> getSupportedKeys() {
        return new ArrayList<>(providers.keySet());
    }

    @Override
    public boolean isSupported(String key) {
        if (key == null) { return false; }
        return providers.containsKey(key);
    }

    @Override
    public Collection<CompletionProvider<?>> getProviders() {
        return new ArrayList<>(providers.values());
    }

    @Override
    public Optional<CompletionProvider<?>> getProviderFor(String key) {
        if (key == null) { return Optional.empty(); }
        return Optional.ofNullable(providers.get(key));
    }

    @Override
    public void registerProvider(CompletionProvider<?> provider) {
        if (isValidKey(provider.key())) {
            // duplicated keys aren't supported, provider must be unregistered
            providers.putIfAbsent(provider.key(), provider);
        }
    }

    @Override
    public void unregisterProvider(String key) {
        if (isValidKey(key)) {
            providers.remove(key);
        }
    }
}
