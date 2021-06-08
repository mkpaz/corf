package org.telekit.base.service.impl;

import org.jetbrains.annotations.NotNull;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.i18n.I18n;
import org.telekit.base.service.Encryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static org.telekit.base.i18n.BaseMessages.MGG_CRYPTO_UNABLE_TO_DECRYPT_DATA;
import static org.telekit.base.i18n.BaseMessages.MGG_CRYPTO_UNABLE_TO_ENCRYPT_DATA;

public abstract class AbstractEncryptor implements Encryptor {

    protected final Algorithm alg;
    protected final int nonceLen;

    protected AbstractEncryptor(Algorithm alg, int nonceLen) {
        this.alg = alg;
        this.nonceLen = nonceLen;
    }

    protected Cipher getCipher(int mode, Key key, byte[] nonce) throws
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(alg.getTransformation());
        IvParameterSpec iv = new IvParameterSpec(nonce);
        cipher.init(mode, key, iv);
        return cipher;
    }

    @Override
    public @NotNull byte[] encrypt(byte[] input, Key key) {
        if (input == null || input.length == 0) return new byte[] {};
        Objects.requireNonNull(key);

        try {
            assertAlgKeySupport(key);

            byte[] nonce = Encryptor.generateNonce(alg);
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, nonce);

            byte[] encodedData = cipher.doFinal(input);

            ByteBuffer result = ByteBuffer.allocate(ALG_ID_LENGTH + nonceLen + encodedData.length)
                    .put(alg.getId())
                    .put(nonce)
                    .put(encodedData);

            return result.array();
        } catch (NoSuchAlgorithmException |
                InvalidKeyException |
                InvalidAlgorithmParameterException |
                NoSuchPaddingException |
                BadPaddingException |
                IllegalBlockSizeException e) {
            throw new TelekitException(I18n.t(MGG_CRYPTO_UNABLE_TO_ENCRYPT_DATA), e);
        }
    }

    @Override
    public @NotNull byte[] decrypt(byte[] input, Key key) {
        if (input == null || input.length == 0) return new byte[] {};
        Objects.requireNonNull(key);

        try {
            assertAlgId(input[0]);
            assertAlgKeySupport(key);

            byte[] nonce = new byte[nonceLen];
            byte[] encodedData = new byte[input.length - ALG_ID_LENGTH - nonceLen];

            System.arraycopy(input, ALG_ID_LENGTH, nonce, 0, nonceLen);
            System.arraycopy(input, ALG_ID_LENGTH + nonceLen, encodedData, 0, encodedData.length);

            Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, nonce);
            return cipher.doFinal(encodedData);
        } catch (NoSuchAlgorithmException |
                InvalidKeyException |
                InvalidAlgorithmParameterException |
                NoSuchPaddingException |
                BadPaddingException |
                IllegalBlockSizeException e) {
            throw new TelekitException(I18n.t(MGG_CRYPTO_UNABLE_TO_DECRYPT_DATA), e);
        }
    }

    @Override
    public @NotNull Algorithm getAlg() {
        return alg;
    }

    protected void assertAlgId(byte algID) throws InvalidAlgorithmParameterException {
        if (algID != alg.getId()) {
            throw new InvalidAlgorithmParameterException("Unsupported algorithm identifier");
        }
    }

    protected void assertAlgKeySupport(Key key) throws InvalidAlgorithmParameterException {
        if (!alg.getKeyAlg().equals(key.getAlgorithm())) {
            throw new InvalidAlgorithmParameterException("Unsupported key algorithm: " + alg.getKeyAlg());
        }
    }
}
