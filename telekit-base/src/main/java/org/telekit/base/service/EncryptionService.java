package org.telekit.base.service;

public interface EncryptionService {

    byte[] encrypt(byte[] input);

    byte[] decrypt(byte[] input);
}
