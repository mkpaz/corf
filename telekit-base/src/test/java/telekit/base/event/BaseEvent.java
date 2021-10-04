package telekit.base.event;

import org.jetbrains.annotations.Nullable;

public class BaseEvent<T> extends Event {

    private final T value;

    public BaseEvent(EventSource source, T value) {
        this.value = value;
    }

    public @Nullable T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "value=" + value +
                "} " + super.toString();
    }
}