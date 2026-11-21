package com.ayyoub.fuzzycoach.fuzzyengine.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The crisp values passed to {@link FuzzyEngine#evaluate(Inputs)}.
 *
 * <p>Immutable and keyed by input variable name. Use {@link #of(String, double)}
 * for one value and {@link #with(String, double)} for the next:
 *
 * <pre>{@code
 * Inputs inputs = Inputs.of("motivation", 8.0).with("stress", 2.0);
 * }</pre>
 */
public final class Inputs {

    private final Map<String, Double> values;

    private Inputs(Map<String, Double> values) {
        this.values = values;
    }

    /**
     * Starts a set of inputs with one value.
     */
    public static Inputs of(String variableName, double value) {
        Map<String, Double> values = new LinkedHashMap<>();
        put(values, variableName, value);

        return new Inputs(values);
    }

    /**
     * Copies an existing map.
     *
     * @throws IllegalArgumentException when the map is null
     */
    public static Inputs from(Map<String, Double> values) {
        if (values == null) {
            throw new IllegalArgumentException("Inputs are required");
        }

        Map<String, Double> copy = new LinkedHashMap<>();
        values.forEach((variableName, value) -> put(copy, variableName, value));

        return new Inputs(copy);
    }

    /**
     * Returns a new set with one more value. An existing name is replaced.
     */
    public Inputs with(String variableName, double value) {
        Map<String, Double> copy = new LinkedHashMap<>(values);
        put(copy, variableName, value);

        return new Inputs(copy);
    }

    /**
     * @return the values by input variable name, in insertion order
     */
    public Map<String, Double> asMap() {
        return Collections.unmodifiableMap(values);
    }

    private static void put(Map<String, Double> target, String variableName, Double value) {
        if (variableName == null || variableName.isBlank()) {
            throw new IllegalArgumentException("An input needs a variable name");
        }
        if (value == null) {
            throw new IllegalArgumentException(
                "Input '" + variableName + "' needs a value");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                "Input '" + variableName + "' must be finite, got " + value);
        }

        target.put(variableName, value);
    }
}
