package corf.base.text;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SequenceMatcherTest {

    @Test
    public void testListWithGapAtTheBeginning() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo3", "foo5", "foo20"))).isEqualTo("foo1");
    }

    @Test
    public void testListWithGapInTheMiddle() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo1", "foo2", "foo5"))).isEqualTo("foo3");
    }

    @Test
    public void testListWithoutGaps() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo1", "foo2", "foo3"))).isEqualTo("foo4");
    }

    @Test
    public void testSingleElementList() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo1"))).isEqualTo("foo2");
        assertThat(m.get(List.of("foo5"))).isEqualTo("foo1");
    }

    @Test
    public void testWhitespaceSeparator() {
        var m = SequenceMatcher.create("foo", " ");
        assertThat(m.get(List.of("foo 1", "foo 2", "foo 3", "foo4"))).isEqualTo("foo 4");
        assertThat(m.get(List.of("foo", "foo 2", "foo 3", "foo4"))).isEqualTo("foo 4");
    }

    @Test
    public void testUnderscoreSeparator() {
        var m = SequenceMatcher.create("foo", "_");
        assertThat(m.get(List.of("foo_1", "foo_2", "foo_3", "foo4"))).isEqualTo("foo_4");
        assertThat(m.get(List.of("foo", "foo_2", "foo_3", "foo4"))).isEqualTo("foo_4");
    }

    @Test
    public void testEmptyList() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(Collections.emptyList())).isEqualTo("foo1");
    }

    @Test
    public void testUnsortedList() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo2", "foo3", "foo1", "foo4"))).isEqualTo("foo5");
    }

    @Test
    public void testStartIndexCanBeOmitted() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo", "foo2"))).isEqualTo("foo3");
    }

    @Test
    public void testOmittedStartIndexNotEndsWithSeparator() {
        var m = SequenceMatcher.create("foo", "_");
        assertThat(m.get(List.of("foo2"), true)).isEqualTo("foo");
    }

    @Test
    public void testStartIndexCanBeOmittedWithSeparator() {
        var m = SequenceMatcher.create("foo", " ");
        assertThat(m.get(List.of("foo", "foo 2"))).isEqualTo("foo 3");
    }

    @Test
    public void testSimilarPatternsIgnored() {
        var m1 = SequenceMatcher.create("foo");
        assertThat(m1.get(List.of("foo1", "foo 2", "foo-2", "foo\t2", "a", "bar"))).isEqualTo("foo2");

        var m2 = SequenceMatcher.create("foo", " ");
        assertThat(m2.get(List.of("foo 1", "foo2", "foo-2", "foo\t2", "fo0"))).isEqualTo("foo 2");
        assertThat(m2.get(List.of("foo", "foo2", "foo-2", "foo\t2", "fo0"))).isEqualTo("foo 2");
    }

    @Test
    public void testListWithDuplicates() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo1", "foo2", "foo2", "foo3", "foo3"))).isEqualTo("foo4");
    }

    @Test
    public void testStartIndex() {
        var m1 = SequenceMatcher.create("foo").setStartIndex(0);
        assertThat(m1.get(List.of("foo2", "foo6", "foo7", "foo8"))).isEqualTo("foo0");

        var m2 = SequenceMatcher.create("foo").setStartIndex(0);
        assertThat(m2.get(List.of("foo0", "foo6", "foo7", "foo8"))).isEqualTo("foo1");

        var m3 = SequenceMatcher.create("foo").setStartIndex(10);
        assertThat(m3.get(List.of())).isEqualTo("foo10");

        var m4 = SequenceMatcher.create("foo").setStartIndex(10);
        assertThat(m4.get(List.of(), true)).isEqualTo("foo");
    }

    @Test
    public void testIndicesLessThanStartAreIgnored() {
        var m1 = SequenceMatcher.create("foo");
        assertThat(m1.get(List.of("foo0", "foo1", "foo2", "foo3"))).isEqualTo("foo4");

        var m2 = SequenceMatcher.create("foo")
                .setStartIndex(3);
        assertThat(m2.get(List.of("foo0", "foo1", "foo2", "foo3"))).isEqualTo("foo4");
    }

    @Test
    public void testObtainingStringWithoutStartIndex() {
        var m1 = SequenceMatcher.create("foo");
        assertThat(m1.get(List.of("foo2", "foo3"), true)).isEqualTo("foo");

        var m2 = SequenceMatcher.create("foo").setStartIndex(10);
        assertThat(m2.get(List.of("foo11", "foo12"), true)).isEqualTo("foo");
    }

    @Test
    public void testMatcherFailsOnStringsWithSuffix() {
        var m = SequenceMatcher.create("foo");
        assertThat(m.get(List.of("foo1bar", "foo2bar", "foo3bar"))).isEqualTo("foo1");
    }

    @Test
    public void testExtractor() {
        var m = SequenceMatcher.<String>create("foo")
                .setExtractor(s -> s.replaceAll("bar", ""));
        assertThat(m.get(List.of("foo1bar", "foo2bar", "foo3bar"))).isEqualTo("foo4");
    }
}
