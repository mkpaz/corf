package telekit.base.event;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public abstract class Event {

    protected final UUID id = UUID.randomUUID();
    protected final EventSource source;

    protected Event() {
        this(null);
    }

    protected Event(EventSource source) {
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public @Nullable EventSource getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Event event = (Event) o;
        return id.equals(event.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", source=" + source +
                '}';
    }

    public boolean isSentBy(EventSource source) {
        return Objects.equals(getSource(), source);
    }
}
