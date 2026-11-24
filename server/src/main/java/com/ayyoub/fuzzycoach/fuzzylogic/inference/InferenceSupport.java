package com.ayyoub.fuzzycoach.fuzzylogic.inference;

import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.Defuzzifier;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Complement;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.SNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.TNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Consequent;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.RuleBase;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Steps shared by the inference strategies.
 */
final class InferenceSupport {

    private InferenceSupport() {}

    static void requireValid(
            List<LinguisticVariable> inputs,
            Map<String, Double> inputValues,
            List<LinguisticVariable> outputs,
            RuleBase ruleBase) {
        Objects.requireNonNull(inputs, "Inputs are required");
        Objects.requireNonNull(inputValues, "Input values are required");
        Objects.requireNonNull(outputs, "Outputs are required");
        Objects.requireNonNull(ruleBase, "Rule base is required");
    }

    /**
     * Fuzzifies the inputs, evaluates the rules, and collects the activation degree of every
     * output term.
     */
    static Map<String, FuzzySet> aggregate(
            List<LinguisticVariable> inputs,
            Map<String, Double> inputValues,
            List<LinguisticVariable> outputs,
            RuleBase ruleBase,
            TNorm and,
            SNorm or,
            Complement complement) {
        Map<String, FuzzySet> fuzzyInputs = new LinkedHashMap<>();

        for (LinguisticVariable input : inputs) {
            Double value = inputValues.get(input.name());

            if (value == null) {
                throw new IllegalArgumentException("Missing value for input: " + input.name());
            }

            fuzzyInputs.put(input.name(), input.fuzzify(value));
        }

        ruleBase.evaluate(fuzzyInputs::get, and, or, complement);

        Map<String, Map<String, Double>> degrees = new LinkedHashMap<>();
        for (LinguisticVariable output : outputs) {
            Map<String, Double> terms = new LinkedHashMap<>();

            for (Term term : output.terms().values()) {
                terms.put(term.name(), 0.0);
            }

            degrees.put(output.name(), terms);
        }

        for (Rule rule : ruleBase.rules()) {
            if (!rule.isTriggered()) {
                continue;
            }

            for (Consequent.Conclusion conclusion : rule.consequent().conclusions()) {
                Map<String, Double> terms = degrees.get(conclusion.variable().name());

                if (terms == null) {
                    throw new IllegalArgumentException(
                            "Unknown output variable: " + conclusion.variable().name());
                }

                terms.merge(conclusion.term().name(), rule.activationDegree(), Math::max);
            }
        }

        Map<String, FuzzySet> result = new LinkedHashMap<>();
        degrees.forEach((name, terms) -> result.put(name, FuzzySet.of(terms)));

        return result;
    }

    static Map<String, Double> defuzzify(
            List<LinguisticVariable> outputs,
            Map<String, FuzzySet> activations,
            Defuzzifier defuzzifier) {
        Map<String, Double> result = new LinkedHashMap<>();

        for (LinguisticVariable output : outputs) {
            result.put(
                    output.name(), defuzzifier.defuzzify(output, activations.get(output.name())));
        }

        return result;
    }

    /**
     * Finds the value whose membership in the term is closest to the activation degree.
     */
    static double inverse(
            LinguisticVariable variable, Term term, double activation, int resolution) {
        double step = (variable.maximum() - variable.minimum()) / resolution;
        double best = variable.minimum();
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i <= resolution; i++) {
            double value = variable.minimum() + i * step;
            double distance = Math.abs(term.membership(value) - activation);

            if (distance < bestDistance) {
                bestDistance = distance;
                best = value;
            }
        }

        return best;
    }

    static double midpoint(LinguisticVariable variable) {
        return (variable.minimum() + variable.maximum()) / 2.0;
    }
}
