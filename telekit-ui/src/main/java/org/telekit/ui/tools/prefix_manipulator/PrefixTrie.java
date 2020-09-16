package org.telekit.ui.tools.prefix_manipulator;

import static java.lang.Character.getNumericValue;

public class PrefixTrie {

    // for a phone number, children are digits 0..9,
    // so max size is 10 and never meant to be changed
    private static final int MAX_CHILDREN = 10;
    private static final int MAX_BITMASK_VALUE = 2 ^ MAX_CHILDREN - 1;

    private Node root;

    public PrefixTrie() {
        this.root = new Node();
    }

    public void insert(String digits) {
        Node currentNode = root;
        int numeral;
        for (int index = 0; index < digits.length(); index++) {
            numeral = getNumericValue(digits.charAt(index));
            Node nextNode = currentNode.getChild(numeral);
            if (nextNode == null) {
                nextNode = new Node();
                currentNode.setChild(numeral, nextNode);
            }
            currentNode = nextNode;
        }
    }

    public boolean delete(String digits) {
        if (digits == null || digits.length() == 0) return false;

        // parent node (and its index) that contains child node
        // that need to be deleted
        Node delBelowNode = this.root;
        int delAfterIndex = 0;

        Node currentNode = this.root;
        int numeral;
        for (int index = 0; index < digits.length(); index++) {
            numeral = getNumericValue(digits.charAt(index));
            Node nextNode = currentNode.getChild(numeral);

            // break, because trie doesn't contain requested digits combo
            if (nextNode == null) return false;

            // remember last node that has more than 1 child
            if (currentNode.size() > 1) {
                delBelowNode = currentNode;
                delAfterIndex = index;
            }

            currentNode = nextNode;
        }

        // remove node or branch
        int indexToDelete = getNumericValue(digits.charAt(delAfterIndex));
        delBelowNode.removeChild(indexToDelete);

        return true;
    }

    public boolean contains(String digits) {
        return digits.equals(getClosest(digits));
    }

    public boolean isEmpty() {
        return this.root.hasChildren();
    }

    public String getClosest(String digits) {
        Node currentNode = this.root;
        int numeral;
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < digits.length(); index++) {
            numeral = getNumericValue(digits.charAt(index));
            Node nextNode = currentNode.getChild(numeral);
            if (nextNode != null) {
                builder.append(numeral);
                currentNode = nextNode;
            } else {
                break;
            }
        }
        return builder.length() != 0 ? builder.toString() : null;
    }

    private static class Node {

        private Node[] children;
        private int mask = 0;
        private boolean isLeaf;

        public Node getChild(int index) {
            return hasChildren() ? this.children[index] : null;
        }

        public void setChild(int index, Node node) {
            if (this.children == null) this.children = new Node[MAX_CHILDREN];
            this.children[index] = node;
            setBit(index);
        }

        public boolean hasChild(int index) {
            return this.children[index] != null;
        }

        public void removeChild(int index) {
            this.children[index] = null;
            unsetBit(index);
        }

        public int size() {
            return Integer.bitCount(mask);
        }

        public boolean hasChildren() {
            return this.mask == 0;
        }

        public boolean isCompleted() {
            return this.mask == MAX_BITMASK_VALUE;
        }

        public void clear() {
            this.children = new Node[MAX_CHILDREN];
            this.mask = 0;
        }

        private void setBit(int index) {
            this.mask |= (1 << index);
        }

        private void unsetBit(int index) {
            this.mask &= ~(1 << index);
        }
    }
}
