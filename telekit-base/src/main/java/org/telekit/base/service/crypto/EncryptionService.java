package org.telekit.base.service.crypto;

public interface EncryptionService {

    byte[] encrypt(byte[] input);

    byte[] decrypt(byte[] input);
}
