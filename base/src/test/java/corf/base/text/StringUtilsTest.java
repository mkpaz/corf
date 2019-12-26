package corf.base.text;

import org.junit.jupiter.api.Test;
import corf.base.OrdinaryTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@OrdinaryTest
public class StringUtilsTest {

    @Test
    public void splitEqually() {
        assertThat(StringUtils.splitEqually(null, 0)).isEmpty();
        assertThat(StringUtils.splitEqually("", 0)).isEmpty();
        assertThat(StringUtils.splitEqually(" ", 1)).isEqualTo(List.of(" "));
        assertThat(StringUtils.splitEqually("  ", 1)).isEqualTo(List.of(" ", " "));

        assertThat(StringUtils.splitEqually("retina", -1)).isEqualTo(List.of("retina"));
        assertThat(StringUtils.splitEqually("retina", 0)).isEqualTo(List.of("retina"));
        assertThat(StringUtils.splitEqually("retina", 2)).isEqualTo(List.of("re", "ti", "na"));
        assertThat(StringUtils.splitEqually("retina", 3)).isEqualTo(List.of("ret", "ina"));
        assertThat(StringUtils.splitEqually("retina", 4)).isEqualTo(List.of("reti", "na"));
        assertThat(StringUtils.splitEqually("retina", 5)).isEqualTo(List.of("retin", "a"));
        assertThat(StringUtils.splitEqually("retina", 6)).isEqualTo(List.of("retina"));
        assertThat(StringUtils.splitEqually("retina", 7)).isEqualTo(List.of("retina"));
        assertThat(StringUtils.splitEqually("retina", 999)).isEqualTo(List.of("retina"));
    }
}