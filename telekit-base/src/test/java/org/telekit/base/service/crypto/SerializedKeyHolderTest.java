package org.telekit.base.service.crypto;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.telekit.base.OrdinaryTest;

import java.security.Key;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.telekit.base.TestUtils.randomEnumValue;
import static org.telekit.base.service.crypto.Encryptor.Algorithm;
import static org.telekit.base.service.crypto.Encryptor.generateKey;

@OrdinaryTest
public class SerializedKeyHolderTest {

    private static final int TEST_COLLECTION_SIZE = 50;

    @ParameterizedTest
    @MethodSource("randomKeyProvider")
    public void testSerialization(Key key) {
        KeyProvider keyProvider = new SerializedKeyHolder(key);
        assertThat(keyProvider.getKey()).isNotNull();
        assertThat(keyProvider.getKey()).isEqualTo(key);
    }

    public static Stream<Key> randomKeyProvider() {
        return Stream.generate(() -> generateKey(randomEnumValue(Algorithm.class)))
                .limit(TEST_COLLECTION_SIZE);
    }
}