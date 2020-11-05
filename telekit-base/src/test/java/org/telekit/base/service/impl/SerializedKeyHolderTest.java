package org.telekit.base.service.impl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.telekit.base.BaseSetup;
import org.telekit.base.service.KeyProvider;

import java.security.Key;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.telekit.base.TestUtils.randomEnumValue;
import static org.telekit.base.service.Encryptor.Algorithm;
import static org.telekit.base.service.Encryptor.generateKey;

@ExtendWith(BaseSetup.class)
public class SerializedKeyHolderTest {

    private static final int TEST_COLLECTION_SIZE = 50;

    @ParameterizedTest
    @MethodSource("randomKeyProvider")
    void testSerialization(Key key) {
        KeyProvider keyProvider = new SerializedKeyHolder(key);
        assertThat(keyProvider.getKey()).isNotNull();
        assertThat(keyProvider.getKey()).isEqualTo(key);
    }

    private static Stream<Key> randomKeyProvider() {
        return Stream.generate(() -> generateKey(randomEnumValue(Algorithm.class)))
                .limit(TEST_COLLECTION_SIZE);
    }
}