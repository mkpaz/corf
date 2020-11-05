package org.telekit.base.service.impl;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.service.KeyProvider;

import java.security.Key;
import java.util.Objects;

public class SerializedKeyHolder implements KeyProvider {

    private final byte[] data;

    public SerializedKeyHolder(Key key) {
        Objects.requireNonNull(key);
        this.data = SerializationUtils.serialize(key);
    }

    @Override
    public @NotNull Key getKey() {
        return SerializationUtils.deserialize(data);
    }
}
