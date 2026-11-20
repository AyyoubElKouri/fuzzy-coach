package com.ayyoub.fuzzycoach.fuzzyengine.core;

import java.util.Map;

/**
 * The fuzzy value of one output variable: one degree per term, after every
 * rule has contributed to it.
 *
 * <p>{@link #membership(double)} rebuilds the resulting fuzzy set by clipping
 * every term at its degree and taking the maximum.
 */
public record FuzzyValue(LinguisticVariable variable, Map<String, Double> degrees) {

    public FuzzyValue {
        if (variable == null) {
            throw new IllegalArgumentException("A fuzzy value needs a variable");
        }
        if (degrees == null) {
            throw new IllegalArgumentException("A fuzzy value needs degrees");
        }

        degrees = Map.copyOf(degrees);
    }

    /**
     * The membership of {@code x} after clipping and merging.
     */
    public double membership(double x) {
        double result = 0.0;

        for (Map.Entry<String, Double> entry : degrees.entrySet()) {
            double activation = entry.getValue();
            if (activation <= 0.0) {
                continue;
            }

            MembershipFunction membership = variable.terms().get(entry.getKey());
            if (membership == null) {
                throw new IllegalArgumentException(
                    "Fuzzy value references unknown term '" + entry.getKey()
                        + "' of variable '" + variable.name() + "'");
            }

            result = Math.max(result, Math.min(activation, membership.degree(x)));
        }

        return result;
    }

    /**
     * True when no rule fired for this variable.
     */
    public boolean isInactive() {
        return degrees.values().stream().noneMatch(degree -> degree > 0.0);
    }
}
