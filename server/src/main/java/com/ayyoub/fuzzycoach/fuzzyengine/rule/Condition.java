package com.ayyoub.fuzzycoach.fuzzyengine.rule;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * A logical condition in a rule premise: either one term, or several
 * conditions combined with AND, OR, or NOT.
 *
 * <p>AND and OR are n-ary because they are associative. NOT binds tighter,
 * so it applies to a single condition.
 */
public sealed interface Condition {

    /**
     * Evaluates this condition against the degrees supplied by {@code lookup}.
     *
     * @param lookup supplies the degree of a simple condition
     * @return the degree of this condition
     */
    double degree(ToDoubleFunction<Simple> lookup);

    /**
     * @return every simple condition in this tree, left to right
     */
    default List<Simple> simpleConditions() {
        return switch (this) {
            case Simple simple -> List.of(simple);
            case And and ->
                and.conditions().stream()
                        .flatMap(condition -> condition.simpleConditions().stream())
                        .toList();
            case Or or ->
                or.conditions().stream()
                        .flatMap(condition -> condition.simpleConditions().stream())
                        .toList();
            case Not not -> not.condition().simpleConditions();
        };
    }

    private static void requireLookup(ToDoubleFunction<Simple> lookup) {
        if (lookup == null) {
            throw new IllegalArgumentException("A degree lookup is required");
        }
    }

    /**
     * One {@code variableName is termName} condition.
     */
    record Simple(String variableName, String termName) implements Condition {

        public Simple {
            if (variableName == null || variableName.isBlank()) {
                throw new IllegalArgumentException("A condition needs a variable name");
            }
            if (termName == null || termName.isBlank()) {
                throw new IllegalArgumentException("A condition needs a term name");
            }
        }

        @Override
        public double degree(ToDoubleFunction<Simple> lookup) {
            requireLookup(lookup);

            return lookup.applyAsDouble(this);
        }

        @Override
        public String toString() {
            return variableName + " is " + termName;
        }
    }

    /**
     * Conditions that must all hold. Its degree is the minimum of its
     * children.
     */
    record And(List<Condition> conditions) implements Condition {

        public And {
            if (conditions == null || conditions.size() < 2) {
                throw new IllegalArgumentException("AND needs at least two conditions");
            }

            conditions = List.copyOf(conditions);
        }

        @Override
        public double degree(ToDoubleFunction<Simple> lookup) {
            requireLookup(lookup);

            return conditions.stream()
                    .mapToDouble(condition -> condition.degree(lookup))
                    .min()
                    .orElseThrow();
        }

        @Override
        public String toString() {
            return conditions.stream()
                    .map(condition ->
                            condition instanceof Or ? "(" + condition + ")" : condition.toString())
                    .collect(Collectors.joining(" AND "));
        }
    }

    /**
     * Conditions where at least one must hold. Its degree is the maximum of
     * its children.
     */
    record Or(List<Condition> conditions) implements Condition {

        public Or {
            if (conditions == null || conditions.size() < 2) {
                throw new IllegalArgumentException("OR needs at least two conditions");
            }

            conditions = List.copyOf(conditions);
        }

        @Override
        public double degree(ToDoubleFunction<Simple> lookup) {
            requireLookup(lookup);

            return conditions.stream()
                    .mapToDouble(condition -> condition.degree(lookup))
                    .max()
                    .orElseThrow();
        }

        @Override
        public String toString() {
            return conditions.stream().map(Condition::toString).collect(Collectors.joining(" OR "));
        }
    }

    /**
     * A negated condition. Its degree is {@code 1 - degree(child)}.
     */
    record Not(Condition condition) implements Condition {

        public Not {
            if (condition == null) {
                throw new IllegalArgumentException("NOT needs a condition");
            }
        }

        @Override
        public double degree(ToDoubleFunction<Simple> lookup) {
            requireLookup(lookup);

            return 1.0 - condition.degree(lookup);
        }
        
        @Override
        public String toString() {
            return condition instanceof Simple simple ? "NOT " + simple : "NOT (" + condition + ")";
        }
    }
}
