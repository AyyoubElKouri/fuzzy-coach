package com.ayyoub.fuzzycoach.fuzzyengine.rule;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Conditions where at least one must hold. Its degree is the maximum of its
 * children.
 */
public record OrCondition(List<Condition> conditions) implements Condition {

    public OrCondition {
        if (conditions == null || conditions.size() < 2) {
            throw new IllegalArgumentException("OR needs at least two conditions");
        }

        conditions = List.copyOf(conditions);
    }

    @Override
    public double degree(ToDoubleFunction<SimpleCondition> lookup) {
        if (lookup == null) {
            throw new IllegalArgumentException("A degree lookup is required");
        }

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
