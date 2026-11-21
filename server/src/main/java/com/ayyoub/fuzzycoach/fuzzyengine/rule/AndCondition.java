package com.ayyoub.fuzzycoach.fuzzyengine.rule;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Conditions that must all hold. Its degree is the minimum of its children.
 */
public record AndCondition(List<Condition> conditions) implements Condition {

    public AndCondition {
        if (conditions == null || conditions.size() < 2) {
            throw new IllegalArgumentException("AND needs at least two conditions");
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
            .min()
            .orElseThrow();
    }

    @Override
    public String toString() {
        return conditions.stream()
            .map(condition -> condition instanceof OrCondition
                ? "(" + condition + ")"
                : condition.toString())
            .collect(Collectors.joining(" AND "));
    }
}
