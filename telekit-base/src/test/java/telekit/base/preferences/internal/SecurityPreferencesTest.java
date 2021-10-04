package telekit.base.preferences.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import telekit.base.OrdinaryTest;
import telekit.base.domain.security.SecuredData;
import telekit.base.util.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@OrdinaryTest
public class SecurityPreferencesTest {

    @Test
    public void testSerialization() throws JsonProcessingException {
        String password = "qwerty";

        SecuredData dataOrig = SecuredData.fromString(password);
        SecurityPreferences securityOrig = new SecurityPreferences();
        securityOrig.setVaultPassword(dataOrig);

        YAMLMapper mapper = Mappers.createYamlMapper();
        String yaml = mapper.writeValueAsString(securityOrig);

        SecurityPreferences securityDest = mapper.readValue(yaml, SecurityPreferences.class);
        assertThat(securityDest.getVaultPassword().getData()).isEqualTo(dataOrig.getData());
    }
}