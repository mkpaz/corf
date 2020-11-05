package org.telekit.base.service;

import org.jetbrains.annotations.NotNull;

public interface EncryptionService {

    @NotNull byte[] encrypt(byte[] input);

    @NotNull byte[] decrypt(byte[] input);
}
