package com.ayyoub.fuzzycoach.fuzzylogic;

import com.ayyoub.fuzzycoach.fuzzylogic.inference.Inference;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.RuleBase;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runs a fuzzy inference system over a set of input variables, output variables, and rules.
 *
 * <p>The engine keeps the input values, runs the configured {@link Inference} strategy, and
 * stores the results.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * FuzzyLogicEngine engine = new FuzzyLogicEngine(inputs, outputs, rules, new Mamdani());
 * engine.setVariable("motivation", 7.0);
 * engine.setVariable("stress", 3.0);
 * engine.evaluate();
 * double intensity = engine.output("intensity");
 * }</pre>
 */
public final class FuzzyLogicEngine {

    private final List<LinguisticVariable> inputs;
    private final List<LinguisticVariable> outputs;
    private final RuleBase ruleBase;
    private final Inference inference;
    private final Map<String, Double> inputValues = new LinkedHashMap<>();
    private Map<String, Double> results;

    /**
     * Creates an engine.
     *
     * @param inputs input variables
     * @param outputs output variables
     * @param ruleBase rules to evaluate
     * @param inference inference strategy
     */
    public FuzzyLogicEngine(
            List<LinguisticVariable> inputs,
            List<LinguisticVariable> outputs,
            RuleBase ruleBase,
            Inference inference) {
        Objects.requireNonNull(inputs, "Inputs are required");
        Objects.requireNonNull(outputs, "Outputs are required");
        Objects.requireNonNull(ruleBase, "Rule base is required");
        Objects.requireNonNull(inference, "Inference is required");

        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.ruleBase = ruleBase;
        this.inference = inference;
    }

    /**
     * Sets the crisp value of an input variable.
     *
     * @param variable name of an input variable
     * @param value crisp value to assign
     */
    public void setVariable(String variable, double value) {
        requireInput(variable);

        inputValues.put(variable, value);
    }

    /**
     * Runs one inference cycle using the current input values.
     */
    public void evaluate() {
        results = inference.infer(inputs, inputValues, outputs, ruleBase);
    }

    /**
     * Returns the defuzzified value of one output variable.
     *
     * @param variable name of an output variable
     * @return the value produced by the last evaluation
     */
    public double output(String variable) {
        requireOutput(variable);

        return results().get(variable);
    }

    /**
     * Returns the defuzzified values of all output variables.
     *
     * @return output variable names mapped to their values from the last evaluation
     */
    public Map<String, Double> outputs() {
        return Collections.unmodifiableMap(results());
    }

    private Map<String, Double> results() {
        if (results == null) {
            throw new IllegalStateException("Engine has not been evaluated");
        }

        return results;
    }

    private void requireInput(String variable) {
        boolean found = inputs.stream().anyMatch(input -> input.name().equals(variable));

        if (!found) {
            throw new IllegalArgumentException("Unknown input variable: " + variable);
        }
    }

    private void requireOutput(String variable) {
        boolean found = outputs.stream().anyMatch(output -> output.name().equals(variable));

        if (!found) {
            throw new IllegalArgumentException("Unknown output variable: " + variable);
        }
    }
}
