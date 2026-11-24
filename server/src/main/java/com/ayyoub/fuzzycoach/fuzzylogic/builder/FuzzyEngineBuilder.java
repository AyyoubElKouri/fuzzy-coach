package com.ayyoub.fuzzycoach.fuzzylogic.builder;

import com.ayyoub.fuzzycoach.fuzzylogic.FuzzyLogicEngine;
import com.ayyoub.fuzzycoach.fuzzylogic.exception.ConfigurationException;
import com.ayyoub.fuzzycoach.fuzzylogic.inference.Inference;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.RuleBase;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent API for constructing an engine from variables, rules, and an inference strategy.
 *
 * <pre>{@code
 * FuzzyLogicEngine engine = FuzzyEngineBuilder.builder()
 *         .input(motivation)
 *         .input(stress)
 *         .output(intensity)
 *         .rule(rule)
 *         .inference(new Mamdani())
 *         .build();
 * }</pre>
 */
public final class FuzzyEngineBuilder {

    private final List<LinguisticVariable> inputs = new ArrayList<>();
    private final List<LinguisticVariable> outputs = new ArrayList<>();
    private final RuleBase ruleBase = new RuleBase();
    private Inference inference;

    private FuzzyEngineBuilder() {}

    /**
     * Creates an empty builder.
     *
     * @return a new builder
     */
    public static FuzzyEngineBuilder builder() {
        return new FuzzyEngineBuilder();
    }

    /**
     * Adds an input variable.
     *
     * @param variable input variable
     * @return this builder
     */
    public FuzzyEngineBuilder input(LinguisticVariable variable) {
        add(inputs, variable, "Input");

        return this;
    }

    /**
     * Adds an output variable.
     *
     * @param variable output variable
     * @return this builder
     */
    public FuzzyEngineBuilder output(LinguisticVariable variable) {
        add(outputs, variable, "Output");

        return this;
    }

    /**
     * Adds a rule.
     *
     * @param rule rule to add
     * @return this builder
     */
    public FuzzyEngineBuilder rule(Rule rule) {
        if (rule == null) {
            throw new ConfigurationException("Rule is required");
        }

        ruleBase.addRule(rule);

        return this;
    }

    /**
     * Adds several rules.
     *
     * @param rules rules to add
     * @return this builder
     */
    public FuzzyEngineBuilder rules(Rule... rules) {
        Objects.requireNonNull(rules, "Rules are required");

        for (Rule rule : rules) {
            rule(rule);
        }

        return this;
    }

    /**
     * Sets the inference strategy.
     *
     * @param inference inference strategy
     * @return this builder
     */
    public FuzzyEngineBuilder inference(Inference inference) {
        this.inference = Objects.requireNonNull(inference, "Inference is required");

        return this;
    }

    /**
     * Validates the configuration and creates the engine.
     *
     * @return a ready engine
     */
    public FuzzyLogicEngine build() {
        if (inputs.isEmpty()) {
            throw new ConfigurationException("At least one input variable is required");
        }

        if (outputs.isEmpty()) {
            throw new ConfigurationException("At least one output variable is required");
        }

        if (ruleBase.rules().isEmpty()) {
            throw new ConfigurationException("At least one rule is required");
        }

        if (inference == null) {
            throw new ConfigurationException("Inference is required");
        }

        return new FuzzyLogicEngine(inputs, outputs, ruleBase, inference);
    }

    private void add(List<LinguisticVariable> target, LinguisticVariable variable, String role) {
        Objects.requireNonNull(variable, role + " variable is required");

        if (variable.terms().isEmpty()) {
            throw new ConfigurationException(role + " variable has no terms: " + variable.name());
        }

        boolean duplicate =
                inputs.stream().anyMatch(existing -> existing.name().equals(variable.name()));

        duplicate = duplicate
                || outputs.stream().anyMatch(existing -> existing.name().equals(variable.name()));

        if (duplicate) {
            throw new ConfigurationException("Duplicate variable: " + variable.name());
        }

        target.add(variable);
    }
}
