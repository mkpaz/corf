package corf.base.collection;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Generates list of maps each entry of which contains a value calculated
 * according to given {@link Rule}.
 * <p>Given rules:</p>
 * <pre>
 *   Rule = { id="A", start=10, step=1, count=2 }
 *   Rule = { id="B", start=0,  step=1, count=2 }
 * </pre>
 * <p>Result:</p>
 * <pre>
 * [
 *   {
 *      { key="A", value=10 },
 *      { key="B", value=0  }
 *   },
 *   {
 *      { key="A", value=10 },
 *      { key="B", value=1  }
 *   },
 *   {
 *      { key="A", value=11 },
 *      { key="B", value=0  }
 *   },
 *   {
 *      { key="A", value=11 },
 *      { key="B", value=1  }
 *   }
 * ]
 * </pre>
 */
public class SequenceGenerator<ID, V> {

    private final List<Rule<ID>> rules;

    // Converter function supposed be used to transform the generator output to any format.
    // (ID, double) -> V, where
    // * ID - rule ID
    // * double - next rule value in sequence
    // * V - calculated output value
    private final BiFunction<ID, Double, V> converter;

    private final Map<ID, V> valuesAccumulator = new HashMap<>();

    public SequenceGenerator(List<Rule<ID>> rules,
                             BiFunction<ID, Double, V> converter) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.converter = Objects.requireNonNull(converter, "converter");
    }

    public List<Map<ID, V>> generate() {
        if (rules.isEmpty()) { return Collections.emptyList(); }

        List<Map<ID, V>> sequence = new ArrayList<>();
        iterate(Objects.requireNonNull(nextRule(null)), sequence);

        return Collections.unmodifiableList(sequence);
    }

    public static <ID> long getExpectedSize(List<Rule<ID>> rules) {
        return rules.stream()
                .map(r -> r.count)
                .reduce(1, (a, b) -> a * b);
    }

    private void iterate(Rule<ID> currentRule, List<Map<ID, V>> sequenceAccumulator) {
        ID id = currentRule.id;
        double value = currentRule.start;
        Rule<ID> nextRule = nextRule(currentRule);

        for (int idx = 0; idx < currentRule.count; idx++) {
            // there's no need to create a new map to accumulate values,
            // because by the end of the recursion cycle all old values
            // will be replaced to the new ones anyway
            valuesAccumulator.put(currentRule.id, converter.apply(id, value));

            // if next rule is not null, go deeper to fill values accumulator
            if (nextRule != null) {
                iterate(nextRule, sequenceAccumulator);
            } else {
                // otherwise, dump values accumulator to resulting sequence
                sequenceAccumulator.add(new HashMap<>(valuesAccumulator));
            }

            value = value + currentRule.step;
        }
    }

    private @Nullable SequenceGenerator.Rule<ID> nextRule(@Nullable Rule<ID> currentRule) {
        // next rule is current rule + 1 or null if there's no next rule
        if (currentRule == null) {
            return rules.get(0);
        }
        int currentIndex = rules.indexOf(currentRule);
        return currentIndex < rules.size() - 1 ? rules.get(currentIndex + 1) : null;
    }

    public static class Rule<ID> {

        public final ID id;
        public final double start;
        public final int step;
        public final int count;

        public Rule(ID id, double start, int step, int count) {
            this.id = Objects.requireNonNull(id);
            this.start = start;
            this.step = step;
            this.count = count;
        }

        @Override
        @SuppressWarnings("EqualsGetClass")
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Rule<?> r = (Rule<?>) o;
            return id.equals(r.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "Rule{" +
                    "id=" + id +
                    ", start=" + start +
                    ", step=" + step +
                    ", count=" + count +
                    '}';
        }
    }
}
