package telekit.base.domain.security;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import telekit.base.OrdinaryTest;
import telekit.base.util.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@OrdinaryTest
public class SecuredDataTest {

    @Test
    public void testSerialization() throws Exception {
        String password = "qwerty";

        SecuredData orig = new SecuredData();
        orig.setData(password.getBytes());

        YAMLMapper mapper = Mappers.createYamlMapper();
        String yaml = mapper.writeValueAsString(orig);

        SecuredData dest = mapper.readValue(yaml, SecuredData.class);
        assertThat(dest.getData()).isEqualTo(password.getBytes());
    }
}