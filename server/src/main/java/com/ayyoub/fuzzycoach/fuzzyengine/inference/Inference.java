package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import com.ayyoub.fuzzycoach.fuzzyengine.rule.Rule;
import com.ayyoub.fuzzycoach.fuzzyengine.variable.LinguisticVariable;

import java.util.List;
import java.util.Map;

/**
 * Public API of the inference module: evaluates a rule base and returns the
 * fuzzy value of every output variable, plus the activation of each rule.
 *
 * <p>{@link InferenceImpl} is the default implementation.
 */
public interface Inference {

    /**
     * @param rules     the rule base, all referencing declared variables
     * @param fuzzified variable name to (term name to degree), from the fuzzifier
     * @param outputs   the declared output variables
     * @return fuzzy values by output variable name, and one activation per rule
     */
    InferenceResult evaluate(
            List<Rule> rules,
            Map<String, Map<String, Double>> fuzzified,
            List<LinguisticVariable> outputs);
}
