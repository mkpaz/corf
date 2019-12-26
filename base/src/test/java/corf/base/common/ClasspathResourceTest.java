package corf.base.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClasspathResourceTest {

    @Test
    public void ensureInvalidPathCannotBeCreated() {
        assertThatThrownBy(() -> ClasspathResource.of(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ClasspathResource.of("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ClasspathResource.of(".")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ClasspathResource.of("./1/2")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ClasspathResource.of("..")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ClasspathResource.of("../..")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ClasspathResource.of("/1/2/~")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void ensureValidPathCanBeCreated() {
        assertThat(String.valueOf(ClasspathResource.of("/"))).isEqualTo("/");
        assertThat(String.valueOf(ClasspathResource.of("_"))).isEqualTo("_");
        assertThat(String.valueOf(ClasspathResource.of("-"))).isEqualTo("-");
        assertThat(String.valueOf(ClasspathResource.of("/_/-"))).isEqualTo("/_/-");
        assertThat(String.valueOf(ClasspathResource.of("/1/2/3"))).isEqualTo("/1/2/3");
        assertThat(String.valueOf(ClasspathResource.of("1/2/3"))).isEqualTo("1/2/3");
        assertThat(String.valueOf(ClasspathResource.of(".file"))).isEqualTo(".file");
        assertThat(String.valueOf(ClasspathResource.of("1/2/.file"))).isEqualTo("1/2/.file");
        assertThat(String.valueOf(ClasspathResource.of("1/2/x.y"))).isEqualTo("1/2/x.y");
        assertThat(String.valueOf(ClasspathResource.of("x.y"))).isEqualTo("x.y");
    }

    @Test
    public void ensureDuplicateAndTrailingSlashesAreOmitted() {
        assertThat(String.valueOf(ClasspathResource.of("//1/2/3"))).isEqualTo("/1/2/3");
        assertThat(String.valueOf(ClasspathResource.of("1////2///3"))).isEqualTo("1/2/3");
        assertThat(String.valueOf(ClasspathResource.of("1/2/3/////"))).isEqualTo("1/2/3");
    }

    @Test
    public void ensurePathsCanBeConcatenated() {
        assertThat(concat("/1/2", "3")).isEqualTo("/1/2/3");
        assertThat(concat("/1/2", "/3")).isEqualTo("/1/2/3");
        assertThat(concat("/1/2/", "3/4/5/6/7/")).isEqualTo("/1/2/3/4/5/6/7");
        assertThat(concat("/1/2/", ".file")).isEqualTo("/1/2/.file");
        assertThat(concat("/1/2/", "x.y")).isEqualTo("/1/2/x.y");
    }

    private String concat(String p1, String p2) {
        return String.valueOf(ClasspathResource.of(p1).concat(p2));
    }
}