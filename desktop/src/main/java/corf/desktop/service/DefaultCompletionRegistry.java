package corf.desktop.service;

import corf.base.preferences.CompletionProvider;
import corf.base.preferences.CompletionRegistry;

import java.util.*;

/** Global registry for completion providers. Use DI to get the instance. */
public class DefaultCompletionRegistry implements CompletionRegistry {

    private final Map<String, CompletionProvider<?>> providers = new HashMap<>();

    @Override
    public Collection<String> getSupportedKeys() {
        return new ArrayList<>(providers.keySet());
    }

    @Override
    public boolean containsKey(String key) {
        return providers.containsKey(key);
    }

    @Override
    public Collection<CompletionProvider<?>> getProviders() {
        return new ArrayList<>(providers.values());
    }

    @Override
    public Optional<CompletionProvider<?>> getProviderFor(String key) {
        return Optional.ofNullable(providers.get(key));
    }

    @Override
    public void registerProvider(CompletionProvider<?> provider) {
        if (CompletionProvider.isValidKey(provider.key())) {
            // duplicated keys aren't supported, provider must be unregistered
            providers.putIfAbsent(provider.key(), provider);
        } else {
            throw new IllegalArgumentException("Auto-completion key can not be blank.");
        }
    }

    @Override
    public void unregisterProvider(String key) {
        if (CompletionProvider.isValidKey(key)) {
            providers.remove(key);
        } else {
            throw new IllegalArgumentException("Auto-completion key can not be blank.");
        }
    }
}
