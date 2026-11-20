package com.ayyoub.fuzzycoach.fuzzyengine;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ayyoub.fuzzycoach.fuzzyengine.core.Conclusion;
import com.ayyoub.fuzzycoach.fuzzyengine.core.FuzzyValue;
import com.ayyoub.fuzzycoach.fuzzyengine.core.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.core.SimpleCondition;
import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.CentroidDefuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.Defuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.Fuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.FuzzifierImpl;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.Inference;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.InferenceImpl;

/**
 * Runs the full Mamdani pipeline in one call:
 * fuzzify, infer, defuzzify.
 *
 * <p>Inference covers both the rule evaluation and the merging of rule
 * conclusions into one fuzzy value per output variable. Defuzzification then
 * turns each of those values into a crisp number.
 *
 * <p>The engine is configured once with its variables and rules, then evaluated
 * many times. Configuration is validated in the constructor, so a typo in a
 * rule fails at startup instead of in the middle of a request.
 *
 * <p>Pure Java on purpose: no framework types cross this boundary.
 */
public class FuzzyEngine {

    private final List<LinguisticVariable> inputs;
    private final List<LinguisticVariable> outputs;
    private final List<Rule> rules;
    private final Fuzzifier fuzzifier;
    private final Inference inference;
    private final Defuzzifier defuzzifier;

    /**
     * Engine using the centroid, the usual Mamdani default.
     */
    public FuzzyEngine(
        List<LinguisticVariable> inputs,
        List<LinguisticVariable> outputs,
        List<Rule> rules) {
        this(inputs, outputs, rules, new CentroidDefuzzifier());
    }

    public FuzzyEngine(
        List<LinguisticVariable> inputs,
        List<LinguisticVariable> outputs,
        List<Rule> rules,
        Defuzzifier defuzzifier) {
        this(inputs, outputs, rules, new FuzzifierImpl(), new InferenceImpl(), defuzzifier);
    }

    public FuzzyEngine(
        List<LinguisticVariable> inputs,
        List<LinguisticVariable> outputs,
        List<Rule> rules,
        Fuzzifier fuzzifier,
        Inference inference,
        Defuzzifier defuzzifier) {

        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("An engine needs at least one input variable");
        }
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("An engine needs at least one output variable");
        }
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("An engine needs at least one rule");
        }
        if (fuzzifier == null || inference == null || defuzzifier == null) {
            throw new IllegalArgumentException("All engine stages are required");
        }

        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.rules = List.copyOf(rules);
        this.fuzzifier = fuzzifier;
        this.inference = inference;
        this.defuzzifier = defuzzifier;

        validate();
    }

    /**
     * @param crispInputs one value per input variable
     * @return the crisp outputs plus the fuzzy value of each output
     */
    public FuzzyResult evaluate(Map<String, Double> crispInputs) {
        Map<String, Map<String, Double>> fuzzified = fuzzifier.fuzzify(inputs, crispInputs);
        Map<String, FuzzyValue> fuzzyValues = inference.evaluate(rules, fuzzified, outputs);

        Map<String, Double> crispOutputs = new LinkedHashMap<>();
        fuzzyValues.forEach((name, value) ->
            crispOutputs.put(name, defuzzifier.defuzzify(value)));

        return new FuzzyResult(crispOutputs, fuzzyValues);
    }

    private void validate() {
        Set<String> inputNames = namesOf(inputs, "input");
        Set<String> outputNames = namesOf(outputs, "output");

        for (String name : outputNames) {
            if (inputNames.contains(name)) {
                throw new IllegalArgumentException(
                    "Variable '" + name + "' is declared both as input and output");
            }
        }

        for (Rule rule : rules) {
            for (SimpleCondition condition : rule.condition().simpleConditions()) {
                requireTerm(
                    inputs,
                    inputNames,
                    condition.variableName(),
                    condition.termName(),
                    "input");
            }

            Conclusion conclusion = rule.conclusion();
            requireTerm(
                outputs,
                outputNames,
                conclusion.variableName(),
                conclusion.termName(),
                "output");
        }
    }

    private Set<String> namesOf(List<LinguisticVariable> variables, String kind) {
        Set<String> names = new LinkedHashSet<>();
        for (LinguisticVariable variable : variables) {
            if (!names.add(variable.name())) {
                throw new IllegalArgumentException(
                    "Duplicate " + kind + " variable: " + variable.name());
            }
        }
        return names;
    }

    private void requireTerm(
        List<LinguisticVariable> variables,
        Set<String> names,
        String variableName,
        String termName,
        String kind) {

        if (!names.contains(variableName)) {
            throw new IllegalArgumentException(
                "Rule references unknown " + kind + " variable: " + variableName);
        }

        LinguisticVariable variable = variables.stream()
            .filter(candidate -> candidate.name().equals(variableName))
            .findFirst()
            .orElseThrow();

        if (!variable.terms().containsKey(termName)) {
            throw new IllegalArgumentException(
                "Variable '" + variableName + "' has no term named '" + termName + "'");
        }
    }
}
