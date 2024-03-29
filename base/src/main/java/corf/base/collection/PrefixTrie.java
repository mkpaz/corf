package corf.base.collection;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** PrefixTrie is a tree-like data structure to operate with phone number prefixes. */
public final class PrefixTrie<T> implements Iterable<PrefixTrie.Entry<T>> {

    // 10 ^ 6 = 1_000_000 nodes, be humble
    public static final int MAX_EXPANSION_DEPTH = 6;

    @SuppressWarnings({ "rawtypes", "NullAway" })
    private static final Node NULL = new Node<>(null, Integer.MIN_VALUE, false);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final Node<T> root = new Node<>(NULL, -1, false);

    public PrefixTrie() { }

    /**
     * Returns trie element that has max match with the specified
     * string or if no matches found.
     */
    public @Nullable Entry<T> findClosest(String digits) {
        checkPhonePrefixFormat(digits);

        Pair<Integer, Node<T>> closestPair = findClosestNode(digits);
        if (closestPair == null) { return null; }

        int closestIndex = closestPair.getLeft();
        Node<T> closestNode = closestPair.getRight();

        var prefix = !prefixHasLength(digits, closestIndex) ? digits.substring(0, closestIndex + 1) : digits;
        return new Entry<>(prefix, closestNode.getValue());
    }

    @VisibleForTesting
    static void checkPhonePrefixFormat(String s) {
        Objects.requireNonNull(s);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("Invalid prefix value +'" + s + "'");
            }
        }
    }

    /**
     * Returns trie element that has maximum match with the specified string, plus,
     * the index of digit that corresponds to that node.
     */
    private @Nullable Pair<Integer, Node<T>> findClosestNode(String digits) {
        Node<T> currentNode = root, lastLeafNode = null;
        int lastLeafIndex = -1, numeral;

        for (int index = 0; index < digits.length(); index++) {
            numeral = getNumeralAt(digits, index);
            currentNode = currentNode.getChild(numeral);
            if (currentNode == null) { break; }
            if (currentNode.isLeaf()) {
                lastLeafIndex = index;
                lastLeafNode = currentNode;
            }
        }

        return lastLeafNode != null ? ImmutablePair.of(lastLeafIndex, lastLeafNode) : null;
    }

    /**
     * Returns trie element that has exact match with the specified
     * string or if no matches found.
     */
    public @Nullable Entry<T> findExact(String digits) {
        checkPhonePrefixFormat(digits);

        Pair<Integer, Node<T>> found = findExactNode(digits);
        return found != null ? new Entry<>(digits, found.getRight().getValue()) : null;
    }

    /**
     * Returns trie element that has exact match with specified string, plus,
     * the index of digit that corresponds that node.
     */
    private @Nullable Pair<Integer, Node<T>> findExactNode(String digits) {
        Pair<Integer, Node<T>> node = findClosestNode(digits);
        return node != null && prefixHasLength(digits, node.getLeft()) ? node : null;
    }

    /**
     * Inserts new prefix into the trie.
     */
    public void add(String digits, @Nullable T value) {
        checkPhonePrefixFormat(digits);

        Node<T> currentNode = root, nextNode;
        int numeral;

        for (int index = 0; index < digits.length(); index++) {
            numeral = getNumeralAt(digits, index);
            nextNode = currentNode.getChild(numeral);
            if (nextNode == null) {
                nextNode = currentNode.setChild(numeral, index == digits.length() - 1);
                nextNode.setValue(value);
            }
            currentNode = nextNode;
        }
    }

    public void add(Entry<T> e) {
        add(e.prefix(), e.value());
    }

    /**
     * Updates value that corresponds to existing prefix.
     */
    public void update(String digits, @Nullable T value) {
        checkPhonePrefixFormat(digits);

        Pair<Integer, Node<T>> found = findExactNode(digits);
        if (found == null) { return; }

        Node<T> nodeToUpdate = found.getRight();
        nodeToUpdate.setValue(value);
    }

    public void update(Entry<T> e) {
        update(e.prefix(), e.value());
    }

    /**
     * Removes existing prefix from trie.
     */
    public void remove(String digits) {
        checkPhonePrefixFormat(digits);

        Pair<Integer, Node<T>> found = findExactNode(digits);
        if (found == null) { return; }

        remove(digits, found);
    }

    private void remove(String digits, Pair<Integer, Node<T>> target) {
        int indexToRemove = target.getLeft();
        Node<T> nodeToRemove = target.getRight();

        if (nodeToRemove.isEmpty()) {
            Node<T> parent = nodeToRemove.getParent();

            Objects.requireNonNull(parent, "Root node cannot be removed.");

            // if this is the last node, then remove everything till first parent node
            // that either leaf or root to avoid dead branches (the ones which have no leaves)
            if (parent.size() == 1) {
                while (!parent.isLeaf() && !parent.isRoot()) {
                    parent = parent.getParent();
                    indexToRemove--;
                }
            }

            parent.removeChild(getNumeralAt(digits, indexToRemove));
        } else {
            // if node contains leaves it become a branch
            nodeToRemove.setLeaf(false);
        }
    }

    /**
     * Extends (expands) existing prefix. E.g. expanding '123' results to:
     * <code>{1230, 1231, 1232, 1233, 1234, 1235, 1236, 1237, 1238, 1239}</code>.
     * <p>
     * If expanded prefix contains any children they may be removed.
     * E.g. expanding '123' with level = 1 will remove '12345' and so on.
     */
    public void expand(String digits, int depth) {
        checkPhonePrefixFormat(digits);

        if (depth <= 0) { return; }

        if (depth > MAX_EXPANSION_DEPTH) {
            throw new IllegalArgumentException("Max expansion depth '" + MAX_EXPANSION_DEPTH + "' exceeded");
        }

        Pair<Integer, Node<T>> found = findExactNode(digits);
        if (found == null) { return; }

        Node<T> nodeToExpand = found.getRight();
        nodeToExpand.setLeaf(false);
        expand(nodeToExpand, depth, nodeToExpand.getValue());
    }

    private void expand(Node<T> node, int depth, @Nullable T value) {
        if (depth == 0) { return; }

        for (int index = 0; index < Node.MAX_CHILDREN; index++) {
            Node<T> child = node.setChild(index, depth == 1);

            if (depth == 1) { child.setValue(value); }

            expand(child, depth - 1, value);
        }
    }

    /**
     * The opposite to the {@link #expand(String, int)}, but for the whole trie.
     * <p>
     * When node collapsed its value will be set to null, if {@code requireValueEquality}
     * is false, and to the value of its children otherwise. So, in latter case the
     * value is promoted up - from children to parent.
     *
     * @param requireValueEquality node will be collapsed only if values of all its
     *                             direct children are equal
     */
    public void collapse(boolean requireValueEquality) {
        final List<Node<T>> bottomCompletedBranches = new ArrayList<>();
        final Predicate<Node<T>> predicate = node ->
                node.isEmpty()
                        && node.isLeaf()
                        && node.getParent() != null
                        && node.getParent().isComplete();
        traverse(getRoot(), predicate, node -> bottomCompletedBranches.add(node.getParent()));

        for (Node<T> node : bottomCompletedBranches) {
            collapse(node, requireValueEquality);
        }
    }

    private void collapse(Node<T> node, boolean requireValueEquality) {
        if (!canBeCollapsed(node, requireValueEquality)) { return; }

        if (requireValueEquality) {
            Node<T> child = node.getChild(0);
            node.setValue(child != null ? child.getValue() : null);
        } else {
            node.setValue(null);
        }

        node.clear();       // remove all children
        node.setLeaf(true); // convert to leaf

        Node<T> parent = node.getParent();
        if (parent != null) { collapse(parent, requireValueEquality); }
    }

    @SuppressWarnings("rawtypes")
    private boolean canBeCollapsed(Node<T> node, boolean requireValueEquality) {
        // if node children size < MAX_CHILDREN, exit immediately
        if (!node.isComplete()) { return false; }

        Node firstChild = Objects.requireNonNull(node.getChild(0), "node");
        for (int index = 0; index < Node.MAX_CHILDREN; index++) {
            Node child = Objects.requireNonNull(node.getChild(index), "node");

            // node can't be collapsed, if at least one of its children isn't leaf
            if (!child.isLeaf()) { return false; }

            // (optionally) node can't be collapsed, if its children values aren't equal
            if (requireValueEquality && !Objects.equals(child.getValue(), firstChild.getValue())) { return false; }
        }

        return true;
    }

    /**
     * Excludes specified prefix from tree. E.g. excluding '123' from '12' results to:
     * <code>{120, 121, 122, 124, 125, 126, 127, 128, 129}</code>.
     * <p>
     * You can only exclude from existing prefixes. In other words, you can't exclude
     * '123' if trie doesn't contain either '12' or '1'.
     */
    public void exclude(String digits) {
        checkPhonePrefixFormat(digits);

        Pair<Integer, Node<T>> closestFound = findClosestNode(digits);
        if (closestFound == null) { return; }

        int closestIndex = closestFound.getLeft();
        Node<T> closestNode = closestFound.getRight();

        // trie already contains node we want to exclude, so we just need to remove it
        if (prefixHasLength(digits, closestIndex)) {
            remove(digits, closestFound);
            return;
        }

        // if not, expand the closest node before removing
        int depth = digits.length() - closestNode.getDigits().length();
        expand(closestNode, depth, closestNode.getValue());
        closestNode.setLeaf(false);
        remove(digits);
    }

    public boolean contains(String digits) {
        checkPhonePrefixFormat(digits);
        return findExactNode(digits) != null;
    }

    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public @NotNull Iterator<Entry<T>> iterator() {
        return new PrefixTrieIterator<>(this);
    }

    @Override
    public Spliterator<Entry<T>> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
    }

    public Stream<Entry<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(getRoot(), -1, sb, true, "");
        return sb.toString();
    }

    private void toString(Node<T> node, int depth, StringBuilder builder, boolean isTailNode, String indentPrefix) {
        if (!node.isRoot()) {
            builder.append(indentPrefix);
            builder.append(isTailNode ? "└── " : "├── ");
            builder.append(node.getNumeral());
            if (node.isLeaf()) { builder.append(" ⃰"); }
            if (node.getValue() != null) { builder.append(" : ").append(node.getValue()); }
        } else {
            builder.append("ROOT");
        }

        builder.append("\n");

        int childrenIndex = 0;
        for (int index = 0; index < Node.MAX_CHILDREN; index++) {
            Node<T> child = node.getChild(index);
            if (child != null) {
                boolean isLastChild = childrenIndex == node.size() - 1;
                String childPrefix = indentPrefix + (isTailNode ? "    " : "│   ");
                // first level is not indented
                toString(child, depth + 1, builder, isLastChild, depth >= 0 ? childPrefix : "");
                childrenIndex++;
            }
        }
    }

    private int getNumeralAt(String digits, int index) {
        int numeral = Character.digit(digits.charAt(index), 10);
        if (numeral < 0 || numeral > 9) {
            throw new IllegalArgumentException("Invalid char: " + "'" + digits.charAt(index) + "' at index " + index);
        }
        return numeral;
    }

    private void traverse(Node<T> node, Predicate<Node<T>> condition, Consumer<Node<T>> consumer) {
        if (condition.test(node)) { consumer.accept(node); }

        for (int index = 0; index < Node.MAX_CHILDREN; index++) {
            Node<T> child = node.getChild(index);
            if (child != null) { traverse(child, condition, consumer); }
        }
    }

    private boolean prefixHasLength(String s, int index) {
        return index == s.length() - 1;
    }

    private Node<T> getRoot() {
        return root;
    }

    ///////////////////////////////////////////////////////////////////////////

    // Represents trie entry. Basically it's just a combination of unique key
    // (prefix) and users payload (value).
    public record Entry<T>(String prefix, @Nullable T value) {

        public Entry {
            Objects.requireNonNull(prefix);
        }

        @Override
        @SuppressWarnings("EqualsGetClass")
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Entry<?> entry = (Entry<?>) o;

            return prefix.equals(entry.prefix);
        }

        @Override
        public int hashCode() {
            return prefix.hashCode();
        }
    }

    // Represent internal trie node.
    private static final class Node<T> {

        // for a phone number, children are digits 0..9
        public static final int MAX_CHILDREN = 10;
        public static final double MAX_BITMASK_VALUE = Math.pow(2, MAX_CHILDREN) - 1;

        private final Node<T> parent;
        private final int numeral;

        private @Nullable T value;
        private boolean isLeaf;

        @SuppressWarnings("rawtypes")
        private @Nullable Node[] children;

        private int mask = 0;

        public Node(Node<T> parent, int numeral, boolean isLeaf) {
            this.parent = parent;
            this.numeral = numeral;
            this.isLeaf = isLeaf;
        }

        @SuppressWarnings("unchecked")
        public @Nullable Node<T> getChild(int index) {
            return !isEmpty() && children != null ? children[index] : null;
        }

        public Node<T> setChild(int index, boolean isLeaf) {
            if (isEmpty()) {
                children = new Node[MAX_CHILDREN];
            }

            Objects.requireNonNull(children, "children");

            Node<T> node = new Node<>(this, index, isLeaf);
            children[index] = node;
            setBit(index);
            return node;
        }

        public void removeChild(int index) {
            if (isEmpty() || children == null) { return; }
            children[index] = null;
            unsetBit(index);
        }

        public void clear() {
            children = new Node[MAX_CHILDREN];
            mask = 0;
        }

        public int size() {
            return Integer.bitCount(mask);
        }

        public boolean isEmpty() {
            return mask == 0;
        }

        public Node<T> getParent() {
            return parent;
        }

        public int getNumeral() {
            return numeral;
        }

        public @Nullable T getValue() {
            return value;
        }

        public void setValue(@Nullable T value) {
            this.value = value;
        }

        public void setLeaf(boolean leaf) {
            isLeaf = leaf;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public boolean isRoot() {
            return parent == NULL;
        }

        public boolean isComplete() {
            return mask == MAX_BITMASK_VALUE;
        }

        public String getDigits() {
            if (isRoot()) { return ""; }

            var sb = new StringBuilder();
            sb.append(numeral);

            Node<T> parent = getParent();
            while (parent != null && !parent.isRoot()) {
                sb.append(parent.getNumeral());
                parent = parent.getParent();
            }

            return sb.reverse().toString();
        }

        private void setBit(int index) {
            mask |= (1 << index);
        }

        private void unsetBit(int index) {
            mask &= ~(1 << index);
        }
    }

    private static final class PrefixTrieIterator<T> implements Iterator<Entry<T>> {

        private final Iterator<Node<T>> nodeIterator;

        public PrefixTrieIterator(PrefixTrie<T> trie) {
            List<Node<T>> accumulator = new ArrayList<>();
            trie.traverse(trie.getRoot(), Node::isLeaf, accumulator::add);
            nodeIterator = accumulator.iterator();
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        @Override
        public Entry<T> next() {
            Node<T> nextNode = nodeIterator.next();
            return new Entry<>(nextNode.getDigits(), nextNode.getValue());
        }
    }
}
