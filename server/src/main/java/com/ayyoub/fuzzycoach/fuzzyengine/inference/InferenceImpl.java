package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import com.ayyoub.fuzzycoach.fuzzyengine.rule.Conclusion;
import com.ayyoub.fuzzycoach.fuzzyengine.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.rule.SimpleCondition;
import com.ayyoub.fuzzycoach.fuzzyengine.variable.FuzzyValue;
import com.ayyoub.fuzzycoach.fuzzyengine.variable.LinguisticVariable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Default implementation of {@link Inference}: merges rule conclusions by
 * taking the maximum degree per output term.
 */
public final class InferenceImpl implements Inference {

    @Override
    public InferenceResult evaluate(
            List<Rule> rules,
            Map<String, Map<String, Double>> fuzzified,
            List<LinguisticVariable> outputs) {

        if (rules == null || fuzzified == null || outputs == null) {
            throw new IllegalArgumentException("Rules, fuzzified values and outputs are required");
        }
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("At least one output variable is required");
        }

        Map<String, Map<String, Double>> degreesByVariable = new LinkedHashMap<>();
        for (LinguisticVariable output : outputs) {
            Map<String, Double> degrees = new LinkedHashMap<>();
            output.terms().keySet().forEach(term -> degrees.put(term, 0.0));
            degreesByVariable.put(output.name(), degrees);
        }

        List<RuleActivation> activations = new ArrayList<>();
        for (Rule rule : rules) {
            double activation = rule.condition().degree(degreeLookup(fuzzified));
            Conclusion conclusion = rule.conclusion();

            Map<String, Double> degrees = degreesByVariable.get(conclusion.variableName());
            if (degrees == null) {
                throw new IllegalArgumentException("Rule concludes variable '"
                        + conclusion.variableName() + "', which is not a declared output");
            }
            if (!degrees.containsKey(conclusion.termName())) {
                throw new IllegalArgumentException("Variable '" + conclusion.variableName()
                        + "' has no term named '" + conclusion.termName() + "'");
            }

            degrees.merge(conclusion.termName(), activation, Math::max);
            activations.add(new RuleActivation(rule, activation));
        }

        Map<String, FuzzyValue> fuzzyValues = new LinkedHashMap<>();
        for (LinguisticVariable output : outputs) {
            fuzzyValues.put(
                    output.name(), new FuzzyValue(output, degreesByVariable.get(output.name())));
        }

        return new InferenceResult(fuzzyValues, activations);
    }

    private ToDoubleFunction<SimpleCondition> degreeLookup(
            Map<String, Map<String, Double>> fuzzified) {

        return condition -> degreeOf(condition, fuzzified);
    }

    private double degreeOf(SimpleCondition condition, Map<String, Map<String, Double>> fuzzified) {

        Map<String, Double> variableDegrees = fuzzified.get(condition.variableName());
        if (variableDegrees == null) {
            throw new IllegalArgumentException("Rule references variable '"
                    + condition.variableName() + "', which has no fuzzified values");
        }

        Double degree = variableDegrees.get(condition.termName());
        if (degree == null) {
            throw new IllegalArgumentException("Variable '" + condition.variableName()
                    + "' has no term named '" + condition.termName() + "'");
        }

        return degree;
    }
}
