package org.telekit.base.telecom;

import org.junit.jupiter.api.Test;
import org.telekit.base.telecom.PrefixTrie.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrefixTrieTest {

    @Test
    public void testPhonePrefixValidation() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();

        assertThatThrownBy(() -> PrefixTrie.requirePhonePrefixFormat(null)).isInstanceOf(NullPointerException.class);
        assertThatNoException().isThrownBy(() -> PrefixTrie.requirePhonePrefixFormat(""));
        assertThatThrownBy(() -> PrefixTrie.requirePhonePrefixFormat("abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PrefixTrie.requirePhonePrefixFormat(" 123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PrefixTrie.requirePhonePrefixFormat("-10")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindClosest() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("12", null);
        trie.add("123", null);
        trie.add("1234", null);
        trie.add("123456789", null);

        assertThat(trie.findClosest("1")).isNull();
        assertThat(trie.findClosest("12")).extracting(Entry::prefix).isEqualTo("12");
        assertThat(trie.findClosest("123")).extracting(Entry::prefix).isEqualTo("123");
        assertThat(trie.findClosest("1234")).extracting(Entry::prefix).isEqualTo("1234");
        assertThat(trie.findClosest("12345")).extracting(Entry::prefix).isEqualTo("1234");
        assertThat(trie.findClosest("123456")).extracting(Entry::prefix).isEqualTo("1234");
        assertThat(trie.findClosest("1234567")).extracting(Entry::prefix).isEqualTo("1234");
        assertThat(trie.findClosest("12345678")).extracting(Entry::prefix).isEqualTo("1234");
        assertThat(trie.findClosest("123456789")).extracting(Entry::prefix).isEqualTo("123456789");
    }

    @Test
    public void testFindExact() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("12", null);
        trie.add("123", null);
        trie.add("1234", null);
        trie.add("123456789", null);

        assertThat(trie.findExact("1")).isNull();
        assertThat(trie.findExact("1234")).extracting(Entry::prefix).isEqualTo("1234");
        assertThat(trie.findExact("123456")).isNull();
        assertThat(trie.findExact("123456789")).extracting(Entry::prefix).isEqualTo("123456789");
    }

    @Test
    public void testAdd() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("", null);
        trie.add("0123456789", 0);
        trie.add("123", 123);
        trie.add("1234", 1234);
        trie.add("1235", 1235);
        trie.add("9", 9);

        assertThat(getPrefixList(trie)).containsExactly("0123456789", "123", "1234", "1235", "9");
        assertThat(trie.findClosest("0123456789")).extracting(Entry::value).isEqualTo(0);
        assertThat(trie.findClosest("123")).extracting(Entry::value).isEqualTo(123);
        assertThat(trie.findClosest("1234")).extracting(Entry::value).isEqualTo(1234);
        assertThat(trie.findClosest("1235")).extracting(Entry::value).isEqualTo(1235);
        assertThat(trie.findClosest("9")).extracting(Entry::value).isEqualTo(9);
    }

    @Test
    public void testUpdate() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("123", 123);
        trie.add("1234", 1234);

        trie.update("1234", 4321);

        assertThat(trie.findClosest("123")).extracting(Entry::value).isEqualTo(123);
        assertThat(trie.findClosest("1234")).extracting(Entry::value).isEqualTo(4321);
    }

    @Test
    public void testRemove() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("0123456789", null);
        trie.add("123", null);
        trie.add("1234", null);
        trie.add("45", null);
        trie.add("46", null);
        trie.add("47", null);
        trie.add("75", null);
        trie.add("755", null);
        trie.add("7550", null);
        trie.add("9", null);

        trie.remove("1");          // remove branch (non-leaf) changes nothing
        trie.remove("0123456789"); // remove long branch
        trie.remove("9");          // remove first-level node
        trie.remove("75");         // remove node which has leaves
        trie.remove("1234");       // remove node which ancestor is also leaf
        trie.remove("46");         // remove node which has sibling leaves, but has no ancestor leaves

        assertThat(getPrefixList(trie)).containsExactly("123", "45", "47", "755", "7550");
    }

    @Test
    public void testContains() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("0123456789", null);
        trie.add("123", null);
        trie.add("1234", null);
        trie.add("1235", null);

        assertTrue(trie.contains("0123456789"));
        assertTrue(trie.contains("123"));
        assertTrue(trie.contains("1234"));
        assertTrue(trie.contains("1235"));

        trie.remove("123");
        trie.remove("1234");

        assertFalse(trie.contains("123"));
        assertFalse(trie.contains("1234"));
    }

    @Test
    public void testIterator() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("0123456789", null);
        trie.add("123", null);
        trie.add("1234", null);
        trie.add("1235", null);

        List<String> prefixes = new ArrayList<>();
        for (Entry<Integer> e : trie) { prefixes.add(e.prefix()); }

        assertThat(getPrefixList(trie)).containsExactly("0123456789", "123", "1234", "1235");
        assertThat(getPrefixList(trie)).isEqualTo(prefixes);
    }

    @Test
    public void testExpand() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        List<String> expectedResult = new ArrayList<>();
        trie.add("123", null);
        trie.add("123456", null);
        trie.add("123456789", null);

        // expand with zero or negative depth changes nothing
        trie.expand("123", -1);
        assertThat(getPrefixList(trie)).containsExactly("123", "123456", "123456789");

        // depth > 1
        trie.expand("123", 2);
        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        expectedResult.addAll(expandPrefix("123", 2));
        assertThat(getPrefixList(trie)).containsExactlyElementsOf(expectedResult);

        // depth = 1
        trie.add("456", null);
        trie.expand("456", 1);
        expectedResult.addAll(expandPrefix("456", 1));
        assertThat(getPrefixList(trie)).containsExactlyElementsOf(expectedResult);

        // one more level down
        trie.expand("4569", 1);
        expectedResult.remove("4569");
        expectedResult.addAll(expandPrefix("4569", 1));
        assertThat(getPrefixList(trie)).containsExactlyElementsOf(expectedResult);

        // expansion remove descendant leaves
        trie.add("9", null);
        trie.add("99", null);
        trie.add("9990123", null);
        trie.expand("9", 1);
        expectedResult.addAll(expandPrefix("9", 1));
        assertThat(getPrefixList(trie)).containsExactlyElementsOf(expectedResult);
        assertFalse(trie.contains("9990123"));
    }

    private List<String> expandPrefix(String prefix, int depth) {
        int minValue = Integer.parseInt(prefix + "0".repeat(depth));
        int maxValue = Integer.parseInt(prefix + "9".repeat(depth));

        List<String> result = new ArrayList<>();
        for (int index = minValue; index <= maxValue; index++) {
            result.add(String.valueOf(index));
        }

        return result;
    }

    @Test
    public void testExpandWithInvalidArgs() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("123", null);
        assertThatThrownBy(() -> trie.expand("123", PrefixTrie.MAX_EXPANSION_DEPTH + 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCollapse() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("1", 1);
        trie.add("123", 123);
        trie.add("1234", 1234);

        // 1M records, should be fast enough
        trie.expand("123", 6);
        assertTrue(trie.contains("123000000"));

        trie.collapse(false);
        assertThat(trie.findExact("123")).extracting(Entry::value).isNull();
        assertThat(getPrefixList(trie)).containsExactly("1", "123");
    }

    @Test
    public void testCollapseRequiringValuesEquality() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("1", 1);
        trie.add("123", 123);
        trie.add("1234", 1234);

        // change node value, so it won't be collapsed
        trie.expand("123", 2);
        trie.update("12300", -1);
        assertTrue(trie.contains("12300"));

        // check nodes 1230* weren't removed
        trie.collapse(true);
        List<String> expectedResult = new ArrayList<>();
        expectedResult.add("1");
        expectedResult.addAll(expandPrefix("123", 1));
        expectedResult.addAll(expandPrefix("1230", 1));
        expectedResult.remove("1230");
        assertThat(getPrefixList(trie)).containsExactlyInAnyOrderElementsOf(expectedResult);

        // update nodes values and collapse the trie
        for (Entry<Integer> e : trie) {
            if (e.prefix().startsWith("123")) { trie.update(e.prefix(), 999); }
        }
        trie.collapse(true);

        assertThat(getPrefixList(trie)).containsExactly("1", "123");
        assertThat(trie.findExact("123")).extracting(Entry::value).isEqualTo(999);
    }

    @Test
    public void testExclude() {
        PrefixTrie<Integer> trie = new PrefixTrie<>();
        trie.add("12", null);

        // depth > 1
        trie.exclude("1234");
        trie.exclude("1239");
        List<String> expectedResult = new ArrayList<>(expandPrefix("12", 2));
        expectedResult.remove("12");
        expectedResult.remove("1234");
        expectedResult.remove("1239");
        assertThat(getPrefixList(trie)).containsExactlyElementsOf(expectedResult);

        // repeat exclude changes nothing
        trie.exclude("1234");
        assertThat(getPrefixList(trie)).containsExactlyElementsOf(expectedResult);

        // excluding branch changes nothing
        trie.exclude("123");
        assertThat(getPrefixList(trie)).containsExactlyElementsOf(expectedResult);

        // one more level down
        trie.exclude("12302");
        expectedResult.addAll(expandPrefix("1230", 1));
        expectedResult.remove("1230");
        expectedResult.remove("12302");
        assertThat(getPrefixList(trie)).containsExactlyInAnyOrderElementsOf(expectedResult); // just check without maintaining order
    }

    private <T> List<String> getPrefixList(PrefixTrie<T> trie) {
        return trie.stream().map(Entry::prefix).collect(Collectors.toList());
    }
}