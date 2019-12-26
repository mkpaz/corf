package corf.base.event;

import backbonefx.event.DefaultEventBus;
import backbonefx.event.Event;

import java.util.function.Consumer;

/** The global event bus. */
public final class Events {

    public static <E extends Event> void listen(Class<? extends E> eventType, Consumer<E> subscriber) {
        getInstance().subscribe(eventType, subscriber);
    }

    public static void fire(Event event) {
        getInstance().publish(event);
    }

    public static DefaultEventBus getInstance() {
        return Events.InstanceHolder.INSTANCE;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final class InstanceHolder {

        private static final DefaultEventBus INSTANCE = new DefaultEventBus();
    }
}
