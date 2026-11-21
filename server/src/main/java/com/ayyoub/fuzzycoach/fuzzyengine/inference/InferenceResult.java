package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import com.ayyoub.fuzzycoach.fuzzyengine.variable.FuzzyValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything one inference pass produced: the fuzzy value of each output, and
 * the activation of every rule that was evaluated.
 */
public record InferenceResult(
        Map<String, FuzzyValue> fuzzyValues, List<RuleActivation> activations) {

    public InferenceResult {
        if (fuzzyValues == null || activations == null) {
            throw new IllegalArgumentException(
                    "An inference result needs fuzzy values and activations");
        }

        fuzzyValues = Collections.unmodifiableMap(new LinkedHashMap<>(fuzzyValues));
        activations = List.copyOf(activations);
    }

    /**
     * The fuzzy value of one output variable.
     *
     * @throws IllegalArgumentException when no such output exists
     */
    public FuzzyValue fuzzyValue(String variableName) {
        FuzzyValue value = fuzzyValues.get(variableName);
        if (value == null) {
            throw new IllegalArgumentException("No output named: " + variableName);
        }

        return value;
    }
}
