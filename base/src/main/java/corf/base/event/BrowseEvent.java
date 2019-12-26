package corf.base.event;

import backbonefx.event.Event;

import java.net.URI;
import java.util.Objects;

public final class BrowseEvent implements Event {

    private final URI uri;

    public BrowseEvent(URI uri) {
        this.uri = Objects.requireNonNull(uri, "uri");
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "BrowseEvent{" +
                "uri=" + uri +
                '}';
    }
}
