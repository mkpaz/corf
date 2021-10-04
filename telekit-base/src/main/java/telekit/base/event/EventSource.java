package telekit.base.event;

/**
 * Represents source of the event.
 *
 * @param name an unique name identifying this event source
 */
public record EventSource(String name) {}