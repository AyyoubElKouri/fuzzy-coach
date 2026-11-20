package com.ayyoub.fuzzycoach.fuzzyengine;

import java.util.Map;

import com.ayyoub.fuzzycoach.fuzzyengine.core.FuzzyValue;

/**
 * Everything the engine produced for one evaluation.
 *
 * <p>The crisp outputs are what callers normally want. The fuzzy value of each
 * output variable is kept so the result can be explained.
 */
public record FuzzyResult(
    Map<String, Double> outputs,
    Map<String, FuzzyValue> fuzzyValues) {

    public FuzzyResult {
        if (outputs == null || fuzzyValues == null) {
            throw new IllegalArgumentException("A result needs outputs and fuzzy values");
        }
        outputs = Map.copyOf(outputs);
        fuzzyValues = Map.copyOf(fuzzyValues);
    }

    /**
     * The crisp value of one output variable.
     */
    public double output(String variable) {
        Double value = outputs.get(variable);
        if (value == null) {
            throw new IllegalArgumentException("No output named: " + variable);
        }
        return value;
    }

    /**
     * The fuzzy value of one output variable.
     */
    public FuzzyValue fuzzyValue(String variable) {
        FuzzyValue value = fuzzyValues.get(variable);
        if (value == null) {
            throw new IllegalArgumentException("No output named: " + variable);
        }
        return value;
    }
}
