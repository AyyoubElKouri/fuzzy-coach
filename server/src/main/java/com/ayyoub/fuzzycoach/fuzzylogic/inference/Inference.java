package com.ayyoub.fuzzycoach.fuzzylogic.inference;

import com.ayyoub.fuzzycoach.fuzzylogic.rule.RuleBase;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

import java.util.List;
import java.util.Map;

/**
 * Evaluates a rule base against input values and produces crisp output values.
 */
public interface Inference {

    /**
     * Runs one inference cycle.
     *
     * @param inputs input variables
     * @param inputValues values indexed by input variable name
     * @param outputs output variables
     * @param ruleBase rules to evaluate
     * @return output values indexed by output variable name
     */
    Map<String, Double> infer(
            List<LinguisticVariable> inputs,
            Map<String, Double> inputValues,
            List<LinguisticVariable> outputs,
            RuleBase ruleBase);
}
