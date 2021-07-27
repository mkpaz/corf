package org.telekit.base.service.completion;

import java.util.Collection;
import java.util.Optional;

public interface CompletionRegistry {

    Collection<String> getSupportedKeys();

    boolean isSupported(String key);

    Collection<CompletionProvider<?>> getProviders();

    Optional<CompletionProvider<?>> getProviderFor(String key);

    void registerProvider(CompletionProvider<?> provider);

    void unregisterProvider(String key);
}
