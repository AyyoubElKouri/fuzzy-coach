package com.ayyoub.fuzzycoach.fuzzylogic.builder;

import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.InvalidRuleException;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Antecedent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Consequent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

/**
 * Fluent API for constructing a {@link Rule}.
 *
 * <pre>{@code
 * Rule rule = Rule.builder()
 *         .when(Antecedent.and(
 *                 Antecedent.of(motivation, motivation.term("high")),
 *                 Antecedent.of(stress, stress.term("low"))))
 *         .then(intensity, intensity.term("high"))
 *         .build();
 * }</pre>
 */
public final class RuleBuilder {

    private Antecedent antecedent;
    private Consequent consequent;

    private RuleBuilder() {}

    /**
     * Creates an empty builder.
     *
     * @return a new builder
     */
    public static RuleBuilder builder() {
        return new RuleBuilder();
    }

    /**
     * Sets the IF part.
     *
     * @param antecedent rule condition
     * @return this builder
     */
    public RuleBuilder when(Antecedent antecedent) {
        if (antecedent == null) {
            throw new ConfigurationException("Antecedent is required");
        }

        this.antecedent = antecedent;

        return this;
    }

    /**
     * Sets the THEN part.
     *
     * @param consequent rule conclusion
     * @return this builder
     */
    public RuleBuilder then(Consequent consequent) {
        if (consequent == null) {
            throw new ConfigurationException("Consequent is required");
        }

        this.consequent = consequent;

        return this;
    }

    /**
     * Sets the THEN part from a variable and a term.
     *
     * @param variable output variable
     * @param term term assigned to the variable
     * @return this builder
     */
    public RuleBuilder then(LinguisticVariable variable, Term term) {
        try {
            this.consequent = new Consequent(variable, term);
        } catch (IllegalArgumentException error) {
            throw new InvalidRuleException(error.getMessage(), error);
        }

        return this;
    }

    /**
     * Adds another conclusion to the THEN part.
     *
     * @param variable output variable
     * @param term term assigned to the variable
     * @return this builder
     */
    public RuleBuilder andThen(LinguisticVariable variable, Term term) {
        if (consequent == null) {
            throw new ConfigurationException("A conclusion is required before andThen");
        }

        try {
            this.consequent = consequent.and(variable, term);
        } catch (IllegalArgumentException error) {
            throw new InvalidRuleException(error.getMessage(), error);
        }

        return this;
    }

    /**
     * Validates the configuration and creates the rule.
     *
     * @return a ready rule
     */
    public Rule build() {
        if (antecedent == null) {
            throw new ConfigurationException("Antecedent is required");
        }

        if (consequent == null) {
            throw new ConfigurationException("Consequent is required");
        }

        return new Rule(antecedent, consequent);
    }
}
