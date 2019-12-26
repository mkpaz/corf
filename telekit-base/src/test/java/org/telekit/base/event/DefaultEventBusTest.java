package org.telekit.base.event;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.MethodName.class)
class DefaultEventBusTest {

    private static final EventSource EVENT_SOURCE = new EventSource(DefaultEventBusTest.class.getCanonicalName());
    private final EventBus eventBus = new DefaultEventBus();

    @Test
    public void testSubscribeWithNullEventTypeThrowsException() {
        assertThatThrownBy(() -> eventBus.subscribe(null, System.out::println))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testSubscribeWithNullConsumerThrowsException() {
        assertThatThrownBy(() -> eventBus.subscribe(StringEvent.class, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testUnsubscribeWithNullEventTypeThrowsException() {
        assertThatThrownBy(() -> eventBus.unsubscribe(null, System.out::println))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testUnsubscribeWithNullConsumerThrowsException() {
        assertThatThrownBy(() -> eventBus.unsubscribe(StringEvent.class, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testPublishingNullEventThrowsException() {
        assertThatThrownBy(() -> eventBus.publish(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testPublishingValidEvents() {
        List<StringEvent> stringEvents = new ArrayList<>();
        eventBus.subscribe(StringEvent.class, stringEvents::add);
        List<NumberEvent> numberEvents = new ArrayList<>();
        eventBus.subscribe(NumberEvent.class, numberEvents::add);

        StringEvent se0 = new StringEvent(EVENT_SOURCE, "foo");
        StringEvent se1 = new StringEvent(EVENT_SOURCE, "bar");
        NumberEvent<Integer> ne0 = new NumberEvent<>(EVENT_SOURCE, 42);
        List.of(se0, se1, ne0).forEach(eventBus::publish);

        assertThat(stringEvents).containsOnly(se0, se1);
        assertThat(numberEvents).containsOnly(ne0);
    }

    @Test
    public void testUnsubscribe() {
        List<StringEvent> stringEvents = new ArrayList<>();
        Consumer<StringEvent> subscriber = stringEvents::add;
        eventBus.subscribe(StringEvent.class, subscriber);

        StringEvent se0 = new StringEvent(EVENT_SOURCE, "foo");
        StringEvent se1 = new StringEvent(EVENT_SOURCE, "bar");
        eventBus.publish(se0);
        eventBus.unsubscribe(subscriber);
        eventBus.publish(se1);

        assertThat(stringEvents).containsOnly(se0);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEventTypeHierarchy() {
        List<IntegerEvent> intEvents = new ArrayList<>();
        Consumer<IntegerEvent> intSubscriber = intEvents::add;
        eventBus.subscribe(IntegerEvent.class, intSubscriber);
        List<NumberEvent> numberEvents = new ArrayList<>();
        Consumer<NumberEvent> numberSubscriber = numberEvents::add;
        eventBus.subscribe(NumberEvent.class, numberSubscriber);
        List<DoubleEvent> doubleEvents = new ArrayList<>();
        Consumer<DoubleEvent> doubleSubscriber = doubleEvents::add;
        eventBus.subscribe(DoubleEvent.class, doubleSubscriber);

        DoubleEvent de0 = new DoubleEvent(EVENT_SOURCE, 0.815);
        IntegerEvent ie0 = new IntegerEvent(EVENT_SOURCE, 42);
        DoubleEvent de1 = new DoubleEvent(EVENT_SOURCE, 0.347);
        IntegerEvent ie1 = new IntegerEvent(EVENT_SOURCE, 7);
        eventBus.publish(de0);
        eventBus.publish(ie0);
        eventBus.unsubscribe(NumberEvent.class, numberSubscriber);
        eventBus.publish(de1);
        eventBus.unsubscribe(IntegerEvent.class, intSubscriber);
        eventBus.publish(ie1);

        assertThat(intEvents).containsOnly(ie0);
        assertThat(numberEvents).containsOnly(de0, ie0);
        assertThat(doubleEvents).containsOnly(de0, de1);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class StringEvent extends BaseEvent<String> {

        public StringEvent(EventSource source, String value) {
            super(source, value);
        }
    }

    private static class NumberEvent<T extends Number> extends BaseEvent<T> {

        public NumberEvent(EventSource source, T value) {
            super(source, value);
        }
    }

    private static class IntegerEvent extends NumberEvent<Integer> {

        public IntegerEvent(EventSource source, Integer value) {
            super(source, value);
        }
    }

    private static class DoubleEvent extends NumberEvent<Double> {

        public DoubleEvent(EventSource source, Double value) {
            super(source, value);
        }
    }
}