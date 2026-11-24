package com.ayyoub.fuzzycoach.fuzzylogic.inference;

import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.Defuzzifier;
import com.ayyoub.fuzzycoach.fuzzylogic.defuzzification.WeightedAverage;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Complement;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Maximum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.Minimum;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.SNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.TNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.rule.RuleBase;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

import java.util.List;
import java.util.Map;

/**
 * Sugeno inference: the activated terms are combined with a weighted average.
 */
public final class Sugeno implements Inference {

    private final Inference delegate;

    public Sugeno() {
        this(new Minimum(), new Maximum(), new Complement());
    }

    /**
     * Creates a Sugeno inference engine.
     *
     * @param and operator used for AND
     * @param or operator used for OR
     * @param complement operator used for NOT
     */
    public Sugeno(TNorm and, SNorm or, Complement complement) {
        Defuzzifier defuzzifier = new WeightedAverage();

        this.delegate = new Mamdani(and, or, complement, defuzzifier);
    }

    @Override
    public Map<String, Double> infer(
            List<LinguisticVariable> inputs,
            Map<String, Double> inputValues,
            List<LinguisticVariable> outputs,
            RuleBase ruleBase) {
        return delegate.infer(inputs, inputValues, outputs, ruleBase);
    }
}
