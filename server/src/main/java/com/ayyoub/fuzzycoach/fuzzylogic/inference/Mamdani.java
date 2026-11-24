package com.ayyoub.fuzzycoach.fuzzylogic.inference;

import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.Centroid;
import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.Defuzzifier;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Complement;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Maximum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Minimum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.SNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.TNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.RuleBase;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mamdani inference: clip each activated term, aggregate with max, then defuzzify.
 */
public final class Mamdani implements Inference {

    private final TNorm and;
    private final SNorm or;
    private final Complement complement;
    private final Defuzzifier defuzzifier;

    public Mamdani() {
        this(new Minimum(), new Maximum(), new Complement(), new Centroid());
    }

    /**
     * Creates a Mamdani inference engine.
     *
     * @param and operator used for AND
     * @param or operator used for OR
     * @param complement operator used for NOT
     * @param defuzzifier converts the aggregated output into a crisp value
     */
    public Mamdani(TNorm and, SNorm or, Complement complement, Defuzzifier defuzzifier) {
        Objects.requireNonNull(and, "And operator is required");
        Objects.requireNonNull(or, "Or operator is required");
        Objects.requireNonNull(complement, "Complement is required");
        Objects.requireNonNull(defuzzifier, "Defuzzifier is required");

        this.and = and;
        this.or = or;
        this.complement = complement;
        this.defuzzifier = defuzzifier;
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

        return InferenceSupport.defuzzify(outputs, activations, defuzzifier);
    }
}
