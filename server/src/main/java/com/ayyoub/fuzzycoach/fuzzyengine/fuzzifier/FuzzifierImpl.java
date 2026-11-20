package com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ayyoub.fuzzycoach.fuzzyengine.core.LinguisticVariable;

/**
 * Default implementation of {@link Fuzzifier}.
 */
public final class FuzzifierImpl implements Fuzzifier {

    @Override
    public Map<String, Double> fuzzify(LinguisticVariable variable, double value) {
        if (variable == null) {
            throw new IllegalArgumentException("Cannot fuzzify against a null variable");
        }

        Map<String, Double> degrees = new LinkedHashMap<>();
        variable.terms().forEach((termName, membership) ->
            degrees.put(termName, membership.degree(value)));

        return degrees;
    }

    @Override
    public Map<String, Map<String, Double>> fuzzify(
        List<LinguisticVariable> variables,
        Map<String, Double> values) {

        if (variables == null || values == null) {
            throw new IllegalArgumentException("Variables and values are required");
        }

        Map<String, Map<String, Double>> results = new LinkedHashMap<>();
        for (LinguisticVariable variable : variables) {
            Double value = values.get(variable.name());
            if (value == null) {
                throw new IllegalArgumentException(
                    "No value provided for variable: " + variable.name());
            }

            results.put(variable.name(), fuzzify(variable, value));
        }

        for (String provided : values.keySet()) {
            boolean known = variables.stream().anyMatch(variable -> variable.name().equals(provided));
            if (!known) {
                throw new IllegalArgumentException("Unknown input variable: " + provided);
            }
        }

        return results;
    }
}
