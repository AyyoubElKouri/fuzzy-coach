package com.ayyoub.fuzzycoach.fuzzylogic.variable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of fuzzifying a crisp value: the degree of membership in every term of a variable.
 */
public final class FuzzySet {

    private final Map<String, Double> degrees;

    private FuzzySet(Map<String, Double> degrees) {
        if (degrees == null) {
            throw new IllegalArgumentException("Degrees are required");
        }

        this.degrees = Collections.unmodifiableMap(new LinkedHashMap<>(degrees));
    }

    /**
     * Creates a fuzzy set from the given term degrees.
     *
     * @param degrees term names mapped to their degrees
     * @return an immutable fuzzy set
     */
    public static FuzzySet of(Map<String, Double> degrees) {
        return new FuzzySet(degrees);
    }

    /**
     * Returns the degree of the given term, or zero when the term is absent.
     *
     * @param term term name
     * @return degree between {@code 0} and {@code 1}
     */
    public double degree(String term) {
        return degrees.getOrDefault(term, 0.0);
    }

    public Map<String, Double> degrees() {
        return degrees;
    }

    public boolean isEmpty() {
        return degrees.isEmpty();
    }
}
