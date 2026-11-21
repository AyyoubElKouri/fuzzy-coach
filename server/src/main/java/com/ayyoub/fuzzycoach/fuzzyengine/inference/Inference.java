package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.FuzzyValue;
import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.LinguisticVariable;

import java.util.List;
import java.util.Map;

/**
 * Public API of the inference stage: evaluates a rule base and returns the
 * fuzzy value of every output variable, plus the activation of each rule.
 *
 * <p>The stage owns the rule language: {@link Rule}, {@link Condition},
 * {@link Conclusion}, and the parser. {@link DefaultInference} is the default
 * implementation.
 */
public interface Inference {

    /**
     * @param rules     the rule base, all referencing declared variables
     * @param fuzzified variable name to its fuzzy value, from the fuzzifier
     * @param outputs   the declared output variables
     * @return fuzzy values by output variable name, and one activation per rule
     */
    InferenceResult evaluate(
            List<Rule> rules, Map<String, FuzzyValue> fuzzified, List<LinguisticVariable> outputs);
}
