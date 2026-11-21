package com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier;

import java.util.List;
import java.util.Map;

/**
 * Public API of the fuzzifier stage: turns crisp input values into degrees of
 * membership.
 *
 * <p>The stage owns the fuzzy vocabulary: {@link LinguisticVariable},
 * {@link MembershipFunction}, {@link Triangle}, and {@link FuzzyValue}.
 * {@link DefaultFuzzifier} is the default implementation.
 *
 * <p>An implementation must reject an unknown or missing input variable, and
 * must return one fuzzy value per supplied variable.
 */
public interface Fuzzifier {

    /**
     * Fuzzifies one crisp value per variable.
     *
     * @param variables the variables to fuzzify, each with at least one term
     * @param values    variable name to crisp value, one entry per variable
     * @return variable name to its fuzzy value, in declaration order
     */
    Map<String, FuzzyValue> fuzzify(List<LinguisticVariable> variables, Map<String, Double> values);
}
