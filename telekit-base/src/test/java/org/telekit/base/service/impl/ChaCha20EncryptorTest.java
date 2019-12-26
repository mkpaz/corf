package org.telekit.base.service.impl;

import org.telekit.base.service.Encryptor;

public class ChaCha20EncryptorTest extends AbstractEncryptorTest {

    public ChaCha20EncryptorTest() {
        super(Encryptor.Algorithm.CHACHA20);
    }
}