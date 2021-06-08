package org.telekit.base.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.i18n.I18n;
import org.telekit.base.service.impl.AesGcmEncryptor;
import org.telekit.base.service.impl.ChaCha20Encryptor;
import org.telekit.base.util.CryptoUtils;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static org.telekit.base.i18n.BaseMessages.MGG_CRYPTO_GENERIC_ERROR;

public interface Encryptor {

    // each algorithm is identified using 1-byte magic number
    byte ALG_ID_LENGTH = 1;

    @NotNull byte[] encrypt(byte[] input, Key key);

    @NotNull byte[] decrypt(byte[] input, Key key);

    @NotNull Algorithm getAlg();

    enum Algorithm {

        AES_GCM((byte) 0, "AES", 256, "AES/GCM/NoPadding", 12),
        CHACHA20((byte) 20, "ChaCha20", 256, "ChaCha20-Poly1305/None/NoPadding", 12);

        private final byte id;
        private final String keyAlg;
        private final int keyLength;   // in bytes
        private final String transformation;
        private final int nonceLength; // in bytes

        Algorithm(byte id, String keyAlg, int keyLength, String transformation, int nonceLength) {
            this.id = id;
            this.keyAlg = keyAlg;
            this.keyLength = keyLength;
            this.transformation = transformation;
            this.nonceLength = nonceLength;
        }

        public byte getId() {
            return id;
        }

        public String getKeyAlg() {
            return keyAlg;
        }

        public int getKeyLength() {
            return keyLength;
        }

        public String getTransformation() {
            return transformation;
        }

        public int getNonceLength() {
            return nonceLength;
        }

        @Override
        public String toString() {
            return "Algorithm{" +
                    "id=" + id +
                    ", keyAlg='" + keyAlg + '\'' +
                    ", keyLength=" + keyLength +
                    ", transformation='" + transformation + '\'' +
                    ", nonceLength=" + nonceLength +
                    "} " + super.toString();
        }
    }

    static Key generateKey(Algorithm alg) {
        Objects.requireNonNull(alg);
        try {
            return CryptoUtils.generateKey(alg.getKeyAlg(), alg.getKeyLength());
        } catch (NoSuchAlgorithmException e) {
            throw new TelekitException(I18n.t(MGG_CRYPTO_GENERIC_ERROR, e));
        }
    }

    static byte[] generateNonce(Algorithm alg) {
        Objects.requireNonNull(alg);
        return CryptoUtils.generateNonce(alg.getNonceLength());
    }

    static @NotNull Encryptor createEncryptor() {
        return createEncryptor(null);
    }

    static @NotNull Encryptor createEncryptor(@Nullable Algorithm alg) {
        if (alg == null) return new AesGcmEncryptor();
        return switch (alg) {
            case AES_GCM -> new AesGcmEncryptor();
            case CHACHA20 -> new ChaCha20Encryptor();
        };
    }
}
