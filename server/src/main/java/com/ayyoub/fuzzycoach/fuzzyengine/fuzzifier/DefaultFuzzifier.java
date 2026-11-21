package com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link Fuzzifier}: evaluates every term of each
 * variable at the crisp input value.
 */
public final class DefaultFuzzifier implements Fuzzifier {

    @Override
    public Map<String, FuzzyValue> fuzzify(
            List<LinguisticVariable> variables, Map<String, Double> values) {

        if (variables == null || values == null) {
            throw new IllegalArgumentException("Variables and values are required");
        }

        Set<String> names = new LinkedHashSet<>();
        for (LinguisticVariable variable : variables) {
            if (variable == null) {
                throw new IllegalArgumentException("A variable is required");
            }
            if (!names.add(variable.name())) {
                throw new IllegalArgumentException("Duplicate variable: " + variable.name());
            }
        }

        for (String provided : values.keySet()) {
            if (!names.contains(provided)) {
                throw new IllegalArgumentException("Unknown input variable: " + provided);
            }
        }

        Map<String, FuzzyValue> fuzzified = new LinkedHashMap<>();
        for (LinguisticVariable variable : variables) {
            Double value = values.get(variable.name());
            if (value == null) {
                throw new IllegalArgumentException(
                        "No value provided for variable: " + variable.name());
            }

            fuzzified.put(variable.name(), fuzzify(variable, value));
        }

        return Collections.unmodifiableMap(fuzzified);
    }

    private FuzzyValue fuzzify(LinguisticVariable variable, double value) {
        Map<String, Double> degrees = new LinkedHashMap<>();
        variable.terms()
                .forEach((termName, membership) -> degrees.put(termName, membership.degree(value)));

        return new FuzzyValue(variable, degrees);
    }
}
