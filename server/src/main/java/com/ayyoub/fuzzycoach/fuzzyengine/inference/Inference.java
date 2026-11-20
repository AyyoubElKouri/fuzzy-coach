package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import java.util.List;
import java.util.Map;

import com.ayyoub.fuzzycoach.fuzzyengine.core.FuzzyValue;
import com.ayyoub.fuzzycoach.fuzzyengine.core.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzyengine.core.Rule;

/**
 * Public API of the inference module: evaluates a rule base and returns the
 * fuzzy value of every output variable.
 *
 * <p>{@link InferenceImpl} is the default implementation.
 */
public interface Inference {

    /**
     * @param rules     the rule base, all referencing declared variables
     * @param fuzzified variable name to (term name to degree), from the fuzzifier
     * @param outputs   the declared output variables
     * @return output variable name to its fuzzy value
     */
    Map<String, FuzzyValue> evaluate(
        List<Rule> rules,
        Map<String, Map<String, Double>> fuzzified,
        List<LinguisticVariable> outputs);
}
