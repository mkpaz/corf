package corf.base.preferences;

import java.util.Collection;
import java.util.Optional;

/** The registry (repository) of the {@link CompletionProvider}'s. */
public interface CompletionRegistry {

    /** Returns keys of all registered providers. */
    Collection<String> getSupportedKeys();

    /** Checks whether provider with the given key registered in the repository. */
    boolean containsKey(String key);

    /** Returns all registered providers. */
    Collection<CompletionProvider<?>> getProviders();

    /** Searches for provider with the given key. */
    Optional<CompletionProvider<?>> getProviderFor(String key);

    void registerProvider(CompletionProvider<?> provider);

    void unregisterProvider(String key);
}
