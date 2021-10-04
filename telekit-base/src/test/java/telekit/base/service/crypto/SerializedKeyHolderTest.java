package telekit.base.service.crypto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import telekit.base.OrdinaryTest;

import java.security.Key;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static telekit.base.service.crypto.Encryptor.Algorithm;
import static telekit.base.service.crypto.Encryptor.generateKey;
import static telekit.tests.util.CommonTestUtils.randomEnumValue;

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