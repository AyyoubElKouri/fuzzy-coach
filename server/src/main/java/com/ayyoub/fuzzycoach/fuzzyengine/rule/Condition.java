package com.ayyoub.fuzzycoach.fuzzyengine.rule;

import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * A logical condition in a rule premise: either one {@link SimpleCondition},
 * or several conditions combined with {@link AndCondition} or
 * {@link OrCondition}.
 */
public sealed interface Condition permits SimpleCondition, AndCondition, OrCondition {

    /**
     * Evaluates this condition against the degrees supplied by {@code lookup}.
     *
     * @param lookup supplies the degree of a simple condition
     * @return the degree of this condition
     */
    double degree(ToDoubleFunction<SimpleCondition> lookup);

    /**
     * @return every simple condition in this tree, left to right
     */
    default List<SimpleCondition> simpleConditions() {
        return switch (this) {
            case SimpleCondition condition -> List.of(condition);
            case AndCondition and ->
                and.conditions().stream()
                        .flatMap(condition -> condition.simpleConditions().stream())
                        .toList();
            case OrCondition or ->
                or.conditions().stream()
                        .flatMap(condition -> condition.simpleConditions().stream())
                        .toList();
        };
    }
}
