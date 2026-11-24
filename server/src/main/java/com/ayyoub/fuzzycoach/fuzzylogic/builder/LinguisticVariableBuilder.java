package com.ayyoub.fuzzycoach.fuzzylogic.builder;

import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.membership.MembershipFunction;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent API for constructing a {@link LinguisticVariable}.
 *
 * <pre>{@code
 * LinguisticVariable motivation = LinguisticVariable.builder()
 *         .named("motivation")
 *         .range(0, 10)
 *         .term("low", new Triangular(0, 0, 5))
 *         .term("high", new Triangular(5, 10, 10))
 *         .build();
 * }</pre>
 */
public final class LinguisticVariableBuilder {

    private String name;
    private Double minimum;
    private Double maximum;
    private final Map<String, MembershipFunction> terms = new LinkedHashMap<>();

    private LinguisticVariableBuilder() {}

    /**
     * Creates an empty builder.
     *
     * @return a new builder
     */
    public static LinguisticVariableBuilder builder() {
        return new LinguisticVariableBuilder();
    }

    /**
     * Sets the variable name.
     *
     * @param name variable name
     * @return this builder
     */
    public LinguisticVariableBuilder named(String name) {
        if (name == null || name.isBlank()) {
            throw new ConfigurationException("Variable name is required");
        }

        this.name = name;

        return this;
    }

    /**
     * Sets the variable range.
     *
     * @param minimum lower bound
     * @param maximum upper bound
     * @return this builder
     */
    public LinguisticVariableBuilder range(double minimum, double maximum) {
        this.minimum = minimum;
        this.maximum = maximum;

        return this;
    }

    /**
     * Adds a term.
     *
     * @param name term name
     * @param function term shape
     * @return this builder
     */
    public LinguisticVariableBuilder term(String name, MembershipFunction function) {
        if (name == null || name.isBlank()) {
            throw new ConfigurationException("Term name is required");
        }

        if (function == null) {
            throw new ConfigurationException("Membership function is required");
        }

        if (terms.putIfAbsent(name, function) != null) {
            throw new ConfigurationException("Duplicate term: " + name);
        }

        return this;
    }

    /**
     * Validates the configuration and creates the variable.
     *
     * @return a ready variable
     */
    public LinguisticVariable build() {
        if (name == null) {
            throw new ConfigurationException("Variable name is required");
        }

        if (minimum == null || maximum == null) {
            throw new ConfigurationException("Range is required for variable: " + name);
        }

        if (terms.isEmpty()) {
            throw new ConfigurationException("At least one term is required for variable: " + name);
        }

        try {
            LinguisticVariable variable = new LinguisticVariable(name, minimum, maximum);
            terms.forEach((term, function) -> variable.addTerm(new Term(term, function)));

            return variable;
        } catch (IllegalArgumentException error) {
            throw new ConfigurationException(error.getMessage(), error);
        }
    }
}
