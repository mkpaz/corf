package org.telekit.base.service.impl;

import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.i18n.I18n;
import org.telekit.base.service.EncryptionService;
import org.telekit.base.service.Encryptor;
import org.telekit.base.service.KeyProvider;

import java.security.Key;
import java.util.Objects;

import static org.telekit.base.Env.DEFAULT_ENCRYPTION_ALG;
import static org.telekit.base.i18n.BaseMessages.*;

public class DefaultEncryptionService implements EncryptionService {

    private final Encryptor encryptor;
    private final KeyProvider keyProvider;

    public DefaultEncryptionService(Encryptor encryptor, KeyProvider keyProvider) {
        this.encryptor = Objects.requireNonNull(encryptor);
        this.keyProvider = Objects.requireNonNull(keyProvider);
    }

    @Override
    public byte[] encrypt(byte[] input) {
        Key key = keyProvider.getKey();
        if (key == null) {
            throw new TelekitException(I18n.concat(MGG_CRYPTO_UNABLE_TO_ENCRYPT_DATA, MGG_CRYPTO_KEY_IS_NOT_PRESENT));
        }
        return encryptor.encrypt(input, key);
    }

    @Override
    public byte[] decrypt(byte[] input) {
        Key key = keyProvider.getKey();
        if (key == null) {
            throw new TelekitException(I18n.concat(MGG_CRYPTO_UNABLE_TO_DECRYPT_DATA, MGG_CRYPTO_KEY_IS_NOT_PRESENT));
        }
        return encryptor.decrypt(input, key);
    }

    public static DefaultEncryptionService create() {
        Encryptor encryptor = Encryptor.createEncryptor(DEFAULT_ENCRYPTION_ALG);
        Key key = Encryptor.generateKey(DEFAULT_ENCRYPTION_ALG);
        KeyProvider keyHolder = new SerializedKeyHolder(key);
        return new DefaultEncryptionService(encryptor, keyHolder);
    }
}
