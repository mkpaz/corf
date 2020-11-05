package org.telekit.base.service;

import org.jetbrains.annotations.Nullable;

import java.security.Key;

public interface KeyProvider {

    @Nullable Key getKey();
}
