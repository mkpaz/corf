package org.telekit.base.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.telekit.base.BaseSetup;
import org.telekit.base.service.Encryptor;
import org.telekit.base.util.PasswordGenerator;

import java.security.Key;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.telekit.base.service.Encryptor.*;

@ExtendWith(BaseSetup.class)
public abstract class AbstractEncryptorTest {

    private static final int TEST_COLLECTION_SIZE = 20;

    protected final Algorithm alg;
    protected final Encryptor encryptor;
    protected final Key key;

    public AbstractEncryptorTest(Algorithm alg) {
        this.alg = alg;
        this.encryptor = createEncryptor(alg);
        this.key = generateKey(alg);
    }

    @ParameterizedTest
    @MethodSource("randomStringProvider")
    public void encrypt_RandomString_DecryptedSuccessfully(String input) {
        byte[] encodedData = encryptor.encrypt(input.getBytes(), key);
        byte[] decodedData = encryptor.decrypt(encodedData, key);
        assertThat(new String(decodedData)).isEqualTo(input);
    }

    @Test
    public void encrypt_EqualStrings_DifferentOutput() {
        String input = "foo bar baz 01234567890";
        byte[] encodedData1 = encryptor.encrypt(input.getBytes(), key);
        byte[] encodedData2 = encryptor.encrypt(input.getBytes(), key);
        assertThat(encodedData1).isNotEqualTo(encodedData2);
    }

    @ParameterizedTest
    @MethodSource("randomStringProvider")
    public void encrypt_WrongDecryptionKey_NotDecrypted(String input) {
        Key decKey = generateKey(alg);
        byte[] encodedData = encryptor.encrypt(input.getBytes(), this.key);
        assertThatThrownBy(() -> encryptor.decrypt(encodedData, decKey)).isInstanceOf(RuntimeException.class);
    }

    public static Stream<String> randomStringProvider() {
        return Stream.generate(() -> PasswordGenerator.random(12, PasswordGenerator.ASCII_ALL))
                .limit(TEST_COLLECTION_SIZE);
    }
}