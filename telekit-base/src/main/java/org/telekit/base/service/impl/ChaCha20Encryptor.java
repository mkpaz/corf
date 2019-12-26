package org.telekit.base.service.impl;

public class ChaCha20Encryptor extends AbstractEncryptor {

    public ChaCha20Encryptor() {
        super(Algorithm.CHACHA20, Algorithm.CHACHA20.getNonceLength());
    }
}
