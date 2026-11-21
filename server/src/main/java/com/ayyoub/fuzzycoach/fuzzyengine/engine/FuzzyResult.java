package com.ayyoub.fuzzycoach.fuzzyengine.engine;

import com.ayyoub.fuzzycoach.fuzzyengine.inference.RuleActivation;
import com.ayyoub.fuzzycoach.fuzzyengine.variable.FuzzyValue;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Everything the engine produced for one evaluation.
 *
 * <p>The crisp outputs are what callers normally want. The fuzzy value of each
 * output variable is kept so the result can be explained, and every rule
 * activation is recorded so the caller can see which rules fired.
 */
public record FuzzyResult(
        Map<String, Double> outputs,
        Map<String, FuzzyValue> fuzzyValues,
        List<RuleActivation> activations) {

    public FuzzyResult {
        if (outputs == null || fuzzyValues == null || activations == null) {
            throw new IllegalArgumentException(
                    "A result needs outputs, fuzzy values and activations");
        }

        outputs = Collections.unmodifiableMap(new LinkedHashMap<>(outputs));
        fuzzyValues = Collections.unmodifiableMap(new LinkedHashMap<>(fuzzyValues));
        activations = List.copyOf(activations);
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
     * The crisp value of one output variable, if it exists.
     */
    public OptionalDouble findOutput(String variable) {
        Double value = outputs.get(variable);

        return value == null ? OptionalDouble.empty() : OptionalDouble.of(value);
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

    /**
     * The rules that fired for one output variable, strongest first.
     *
     * @param variableName an output variable of this result
     * @return the fired rules, empty when none fired
     * @throws IllegalArgumentException when no such output exists
     */
    public List<RuleActivation> explanation(String variableName) {
        fuzzyValue(variableName);

        return activations.stream()
                .filter(activation ->
                        activation.rule().conclusion().variableName().equals(variableName))
                .filter(activation -> activation.activation() > 0.0)
                .sorted(Comparator.comparingDouble(RuleActivation::activation).reversed())
                .toList();
    }
}
