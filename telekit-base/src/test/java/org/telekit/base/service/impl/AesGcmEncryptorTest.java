package org.telekit.base.service.impl;

import org.telekit.base.service.Encryptor;

class AesGcmEncryptorTest extends AbstractEncryptorTest {

    public AesGcmEncryptorTest() {
        super(Encryptor.Algorithm.AES_GCM);
    }
}