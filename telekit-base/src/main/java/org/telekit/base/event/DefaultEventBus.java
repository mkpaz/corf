package org.telekit.base.event;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * Simple event bus implementation.
 * <p>
 * Subscribe and publish events. Events are published in channels distinguished by event type.
 * Channels can be grouped using an event type hierarchy.
 * <p>
 * You can use the default event bus instance {@link #getInstance}, which is a singleton
 * or you can create one or multiple instances of {@link DefaultEventBus}.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class DefaultEventBus implements EventBus {

    public DefaultEventBus() {}

    private static class InstanceHolder {

        private static final DefaultEventBus INSTANCE = new DefaultEventBus();
    }

    public static DefaultEventBus getInstance() {
        return DefaultEventBus.InstanceHolder.INSTANCE;
    }

    private final Map<Class<?>, Set<Consumer>> subscribers = new ConcurrentHashMap<>();

    @Override
    public <T> void subscribe(Class<? extends T> eventType, Consumer<T> subscriber) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(subscriber, "subscriber");

        Set<Consumer> eventSubscribers = getOrCreateSubscribers(eventType);
        eventSubscribers.add(subscriber);
    }

    private <T> Set<Consumer> getOrCreateSubscribers(Class<T> eventType) {
        Set<Consumer> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers == null) {
            eventSubscribers = new CopyOnWriteArraySet<>();
            subscribers.put(eventType, eventSubscribers);
        }
        return eventSubscribers;
    }

    @Override
    public void unsubscribe(Consumer<?> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber");

        subscribers.values().forEach(eventSubscribers -> eventSubscribers.remove(subscriber));
    }

    @Override
    public <T> void unsubscribe(Class<? extends T> eventType, Consumer<T> subscriber) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(subscriber, "subscriber");

        subscribers.keySet().stream()
                .filter(eventType::isAssignableFrom)
                .map(subscribers::get)
                .forEach(eventSubscribers -> eventSubscribers.remove(subscriber));
    }

    @Override
    public void publish(Object event) {
        Objects.requireNonNull(event, "event");

        Class<?> eventType = event.getClass();
        subscribers.keySet().stream()
                .filter(type -> type.isAssignableFrom(eventType))
                .flatMap(type -> subscribers.get(type).stream())
                .forEach(subscriber -> publish(event, subscriber));
    }

    private static void publish(Object event, Consumer subscriber) {
        try {
            subscriber.accept(event);
        } catch (Exception e) {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }
}
