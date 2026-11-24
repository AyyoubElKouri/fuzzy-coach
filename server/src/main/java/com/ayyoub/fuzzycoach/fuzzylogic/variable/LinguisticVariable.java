package com.ayyoub.fuzzycoach.fuzzylogic.variable;

import com.ayyoub.fuzzycoach.fuzzylogic.builder.LinguisticVariableBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A variable with a numeric range and a set of linguistic terms.
 */
public final class LinguisticVariable {

    private final String name;
    private final double minimum;
    private final double maximum;
    private final Map<String, Term> terms = new LinkedHashMap<>();

    /**
     * Creates a linguistic variable.
     *
     * @param name variable name
     * @param minimum lower bound of the range
     * @param maximum upper bound of the range, greater than {@code minimum}
     */
    public LinguisticVariable(String name, double minimum, double maximum) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Variable name is required");
        }

        if (!(minimum < maximum)) {
            throw new IllegalArgumentException("Minimum must be less than maximum");
        }

        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Creates a fluent builder for a linguistic variable.
     *
     * @return a new builder
     */
    public static LinguisticVariableBuilder builder() {
        return LinguisticVariableBuilder.builder();
    }

    /**
     * Adds a term to this variable.
     *
     * @param term term to add
     */
    public void addTerm(Term term) {
        if (term == null) {
            throw new IllegalArgumentException("Term is required");
        }

        if (terms.containsKey(term.name())) {
            throw new IllegalArgumentException("Duplicate term: " + term.name());
        }

        terms.put(term.name(), term);
    }

    /**
     * Fuzzifies a crisp value against every term of this variable.
     *
     * @param value crisp value
     * @return degree of membership in each term
     */
    public FuzzySet fuzzify(double value) {
        Map<String, Double> degrees = new LinkedHashMap<>();

        for (Term term : terms.values()) {
            degrees.put(term.name(), term.membership(value));
        }

        return FuzzySet.of(degrees);
    }

    /**
     * Returns the term with the given name.
     *
     * @param term term name
     * @return the term
     */
    public Term term(String term) {
        Term result = terms.get(term);

        if (result == null) {
            throw new IllegalArgumentException("Unknown term: " + term);
        }

        return result;
    }

    public String name() {
        return name;
    }

    public double minimum() {
        return minimum;
    }

    public double maximum() {
        return maximum;
    }

    public Map<String, Term> terms() {
        return Collections.unmodifiableMap(terms);
    }
}
