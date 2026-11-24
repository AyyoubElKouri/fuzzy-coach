package com.ayyoub.fuzzycoach.fuzzylogic.inference;

import com.ayyoub.fuzzycoach.fuzzylogic.operator.Complement;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Maximum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Minimum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.SNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.TNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.RuleBase;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Tsukamoto inference: inverts the membership of each activated term and averages the results,
 * weighted by the activation degree.
 */
public final class Tsukamoto implements Inference {

    private static final int DEFAULT_RESOLUTION = 100;

    private final TNorm and;
    private final SNorm or;
    private final Complement complement;
    private final int resolution;

    public Tsukamoto() {
        this(new Minimum(), new Maximum(), new Complement(), DEFAULT_RESOLUTION);
    }

    /**
     * Creates a Tsukamoto inference engine.
     *
     * @param and operator used for AND
     * @param or operator used for OR
     * @param complement operator used for NOT
     * @param resolution number of samples used to invert the membership functions
     */
    public Tsukamoto(TNorm and, SNorm or, Complement complement, int resolution) {
        Objects.requireNonNull(and, "And operator is required");
        Objects.requireNonNull(or, "Or operator is required");
        Objects.requireNonNull(complement, "Complement is required");

        if (resolution <= 0) {
            throw new IllegalArgumentException("Resolution must be greater than zero");
        }

        this.and = and;
        this.or = or;
        this.complement = complement;
        this.resolution = resolution;
    }

    @Override
    public Map<String, Double> infer(
            List<LinguisticVariable> inputs,
            Map<String, Double> inputValues,
            List<LinguisticVariable> outputs,
            RuleBase ruleBase) {
        InferenceSupport.requireValid(inputs, inputValues, outputs, ruleBase);

        Map<String, FuzzySet> activations = InferenceSupport.aggregate(
                inputs, inputValues, outputs, ruleBase, and, or, complement);

        Map<String, Double> result = new LinkedHashMap<>();
        for (LinguisticVariable output : outputs) {
            result.put(output.name(), defuzzify(output, activations.get(output.name())));
        }

        return result;
    }

    private double defuzzify(LinguisticVariable output, FuzzySet degrees) {
        double numerator = 0.0;
        double denominator = 0.0;

        for (Map.Entry<String, Double> entry : degrees.degrees().entrySet()) {
            double activation = entry.getValue();

            if (activation <= 0.0) {
                continue;
            }

            Term term = output.term(entry.getKey());
            double value = InferenceSupport.inverse(output, term, activation, resolution);
            numerator += activation * value;
            denominator += activation;
        }

        if (denominator == 0.0) {
            return InferenceSupport.midpoint(output);
        }

        return numerator / denominator;
    }
}
