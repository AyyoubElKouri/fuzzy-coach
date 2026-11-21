package com.ayyoub.fuzzycoach.fuzzyengine;

import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.CentroidDefuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier.Defuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.DefaultFuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.Fuzzifier;
import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.FuzzyValue;
import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.Conclusion;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.Condition;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.DefaultInference;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.Inference;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.InferenceResult;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.inference.RuleSyntaxException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Runs the full Mamdani pipeline in one call:
 * fuzzify, infer, defuzzify.
 *
 * <p>Each stage lives in its own package behind one public interface:
 * {@link Fuzzifier}, {@link Inference}, and {@link Defuzzifier}. This class
 * wires them together, validates the configuration, and hands the result back.
 *
 * <p>Inference covers both the rule evaluation and the merging of rule
 * conclusions into one fuzzy value per output variable. Defuzzification then
 * turns each of those values into a crisp number.
 *
 * <p>Build an engine once with {@link #builder()}, then evaluate it many times.
 * The engine is immutable and safe to share between threads. Configuration is
 * validated when it is built, so a typo in a rule fails at startup instead of
 * in the middle of a request.
 *
 * <pre>{@code
 * FuzzyEngine engine = FuzzyEngine.builder()
 *     .input(motivation)
 *     .input(stress)
 *     .output(intensity)
 *     .rules(
 *         "IF motivation is low THEN intensity is low",
 *         "IF motivation is high AND stress is low THEN intensity is high")
 *     .build();
 *
 * FuzzyResult result = engine.evaluate(Inputs.of("motivation", 8.0).with("stress", 2.0));
 * }</pre>
 *
 * <p>Pure Java on purpose: no framework types cross this boundary.
 */
public final class FuzzyEngine {

    private final List<LinguisticVariable> inputs;
    private final List<LinguisticVariable> outputs;
    private final List<Rule> rules;
    private final Fuzzifier fuzzifier;
    private final Inference inference;
    private final Defuzzifier defuzzifier;

    private FuzzyEngine(
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
     * Starts a fluent engine definition. The centroid defuzzifier and the
     * default fuzzifier and inference stages are used unless overridden.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param inputs one value per input variable
     * @return the crisp outputs, the fuzzy value of each output, and the rule
     *         activations
     */
    public FuzzyResult evaluate(Inputs inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("Inputs are required");
        }

        Map<String, Double> values = inputs.asMap();
        requireCompleteInputs(values);

        Map<String, FuzzyValue> fuzzified = fuzzifier.fuzzify(this.inputs, values);
        InferenceResult inferred = inference.evaluate(rules, fuzzified, outputs);
        Map<String, FuzzyValue> fuzzyValues = inferred.fuzzyValues();

        Map<String, Double> crispOutputs = new LinkedHashMap<>();
        fuzzyValues.forEach((name, value) -> crispOutputs.put(name, defuzzifier.defuzzify(value)));

        return new FuzzyResult(crispOutputs, fuzzyValues, inferred.activations());
    }

    /**
     * Convenience overload for callers that already have a map.
     *
     * @see #evaluate(Inputs)
     */
    public FuzzyResult evaluate(Map<String, Double> crispInputs) {
        return evaluate(Inputs.from(crispInputs));
    }

    private void requireCompleteInputs(Map<String, Double> values) {
        Set<String> inputNames = new LinkedHashSet<>();
        for (LinguisticVariable input : inputs) {
            inputNames.add(input.name());
        }

        for (String provided : values.keySet()) {
            if (!inputNames.contains(provided)) {
                throw new IllegalArgumentException(
                        "Unknown input variable: " + provided + ", expected one of " + inputNames);
            }
        }

        for (String expected : inputNames) {
            if (!values.containsKey(expected)) {
                throw new IllegalArgumentException("Missing value for input variable: " + expected);
            }
        }
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
            for (Condition.Simple condition : rule.condition().simpleConditions()) {
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

    /**
     * Builds a {@link FuzzyEngine} one input, output, and rule at a time.
     */
    public static final class Builder {

        private final List<LinguisticVariable> inputs = new ArrayList<>();
        private final List<LinguisticVariable> outputs = new ArrayList<>();
        private final List<Rule> rules = new ArrayList<>();
        private Fuzzifier fuzzifier = new DefaultFuzzifier();
        private Inference inference = new DefaultInference();
        private Defuzzifier defuzzifier = new CentroidDefuzzifier();

        private Builder() {}

        public Builder input(LinguisticVariable variable) {
            if (variable == null) {
                throw new IllegalArgumentException("An input variable is required");
            }

            inputs.add(variable);

            return this;
        }

        public Builder inputs(List<LinguisticVariable> variables) {
            if (variables == null) {
                throw new IllegalArgumentException("Input variables are required");
            }

            inputs.addAll(variables);

            return this;
        }

        public Builder output(LinguisticVariable variable) {
            if (variable == null) {
                throw new IllegalArgumentException("An output variable is required");
            }

            outputs.add(variable);

            return this;
        }

        public Builder outputs(List<LinguisticVariable> variables) {
            if (variables == null) {
                throw new IllegalArgumentException("Output variables are required");
            }

            outputs.addAll(variables);

            return this;
        }

        public Builder rule(Rule rule) {
            if (rule == null) {
                throw new IllegalArgumentException("A rule is required");
            }

            rules.add(rule);

            return this;
        }

        public Builder rules(Rule... rules) {
            if (rules == null) {
                throw new IllegalArgumentException("Rules are required");
            }

            for (Rule rule : rules) {
                rule(rule);
            }

            return this;
        }

        public Builder rules(List<Rule> rules) {
            if (rules == null) {
                throw new IllegalArgumentException("Rules are required");
            }

            rules.forEach(this::rule);

            return this;
        }

        /**
         * Parses each text in order. A syntax error reports which rule failed.
         */
        public Builder rules(String... texts) {
            if (texts == null) {
                throw new IllegalArgumentException("Rule texts are required");
            }

            for (int index = 0; index < texts.length; index++) {
                try {
                    rules.add(new Rule(texts[index]));
                } catch (RuleSyntaxException error) {
                    throw new RuleSyntaxException(
                            "rule " + (index + 1) + " of " + texts.length + ": " + error.detail(),
                            error.offset(),
                            error.ruleText());
                }
            }

            return this;
        }

        public Builder fuzzifier(Fuzzifier fuzzifier) {
            if (fuzzifier == null) {
                throw new IllegalArgumentException("A fuzzifier is required");
            }

            this.fuzzifier = fuzzifier;

            return this;
        }

        public Builder inference(Inference inference) {
            if (inference == null) {
                throw new IllegalArgumentException("An inference stage is required");
            }

            this.inference = inference;

            return this;
        }

        public Builder defuzzifier(Defuzzifier defuzzifier) {
            if (defuzzifier == null) {
                throw new IllegalArgumentException("A defuzzifier is required");
            }

            this.defuzzifier = defuzzifier;

            return this;
        }

        /**
         * @throws IllegalArgumentException when a variable, rule, or reference is invalid
         */
        public FuzzyEngine build() {
            return new FuzzyEngine(inputs, outputs, rules, fuzzifier, inference, defuzzifier);
        }
    }
}
