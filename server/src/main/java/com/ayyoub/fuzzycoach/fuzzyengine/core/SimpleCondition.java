package com.ayyoub.fuzzycoach.fuzzyengine.core;

import java.util.function.ToDoubleFunction;

/**
 * One {@code variableName is termName} condition.
 */
public record SimpleCondition(String variableName, String termName)
    implements Condition {

    public SimpleCondition {
        if (variableName == null || variableName.isBlank()) {
            throw new IllegalArgumentException("A condition needs a variable name");
        }
        if (termName == null || termName.isBlank()) {
            throw new IllegalArgumentException("A condition needs a term name");
        }
    }

    @Override
    public double degree(ToDoubleFunction<SimpleCondition> lookup) {
        if (lookup == null) {
            throw new IllegalArgumentException("A degree lookup is required");
        }

        return lookup.applyAsDouble(this);
    }

    @Override
    public String toString() {
        return variableName + " is " + termName;
    }
}
