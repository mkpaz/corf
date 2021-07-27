package org.telekit.base.service.crypto;

import org.apache.commons.lang3.SerializationUtils;

import java.security.Key;
import java.util.Objects;

public class SerializedKeyHolder implements KeyProvider {

    private final byte[] data;

    public SerializedKeyHolder(Key key) {
        Objects.requireNonNull(key);
        this.data = SerializationUtils.serialize(key);
    }

    @Override
    public Key getKey() {
        return SerializationUtils.deserialize(data);
    }
}
