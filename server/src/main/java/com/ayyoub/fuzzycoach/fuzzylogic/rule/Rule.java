package com.ayyoub.fuzzycoach.fuzzylogic.rule;

import com.ayyoub.fuzzycoach.fuzzylogic.builder.RuleBuilder;

import java.util.Objects;

/**
 * A fuzzy rule: an antecedent and a consequent.
 */
public final class Rule {

    private final Antecedent antecedent;
    private final Consequent consequent;
    private double activationDegree;

    /**
     * Creates a rule.
     *
     * @param antecedent IF part
     * @param consequent THEN part
     */
    public Rule(Antecedent antecedent, Consequent consequent) {
        Objects.requireNonNull(antecedent, "Antecedent is required");
        Objects.requireNonNull(consequent, "Consequent is required");

        this.antecedent = antecedent;
        this.consequent = consequent;
    }

    /**
     * Creates a fluent builder for a rule.
     *
     * @return a new builder
     */
    public static RuleBuilder builder() {
        return RuleBuilder.builder();
    }

    public Antecedent antecedent() {
        return antecedent;
    }

    public Consequent consequent() {
        return consequent;
    }

    public double activationDegree() {
        return activationDegree;
    }

    public boolean isTriggered() {
        return activationDegree > 0.0;
    }

    /**
     * Stores the degree produced by evaluating this rule.
     *
     * @param degree activation degree between {@code 0} and {@code 1}
     */
    public void activate(double degree) {
        if (!(degree >= 0.0 && degree <= 1.0)) {
            throw new IllegalArgumentException("Activation degree must be between 0 and 1");
        }

        this.activationDegree = degree;
    }
}
