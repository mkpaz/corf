package org.telekit.base.util;

import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.Messages;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Destroyable;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.telekit.base.i18n.BaseMessageKeys.MGG_CRYPTO_GENERIC_ERROR;

public final class CryptoUtils {

    public static SecretKeySpec generateKey(String keyAlg, int keyLength) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(keyAlg);
        keyGen.init(keyLength, SecureRandom.getInstanceStrong());
        SecretKey secretKey = keyGen.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), keyAlg);
    }

    /**
     * This method creates the nonce which is required for some algorithms (like ChaCha20).
     * The nonce is not a secret. The only requirement being it has to be unique for a given key.
     *
     * @param len nonce length in bytes
     */
    public static byte[] generateNonce(int len) {
        byte[] newNonce = new byte[len];
        new SecureRandom().nextBytes(newNonce);
        return newNonce;
    }

    /**
     * Strings should not be used to hold the clear text message or the key, as
     * Strings go in the String pool and they will show up in a heap dump. For the
     * same reason, the client calling encryption or decryption methods
     * should clear all the variables or arrays holding the message or the key
     * after they are no longer needed. Since Java 8 does not provide an easy
     * mechanism to clear the key from {@code SecretKeySpec}, this method uses
     * reflection to provide an option to clear the key.
     *
     * @param key the secret key used to do the encryption
     */
    public static void clearSecret(Destroyable key) throws
            IllegalArgumentException,
            IllegalAccessException,
            NoSuchFieldException,
            SecurityException {
        Field keyField = key.getClass().getDeclaredField("key");
        keyField.setAccessible(true);
        byte[] encodedKey = (byte[]) keyField.get(key);
        Arrays.fill(encodedKey, Byte.MIN_VALUE);
    }

    public static byte[] sha256(byte[] source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(source);
        } catch (NoSuchAlgorithmException e) {
            throw new TelekitException(Messages.get(MGG_CRYPTO_GENERIC_ERROR), e);
        }
    }
}
