package com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier;

import java.util.List;
import java.util.Map;

import com.ayyoub.fuzzycoach.fuzzyengine.core.LinguisticVariable;

/**
 * Public API of the fuzzifier module: turns crisp values into degrees of
 * membership.
 *
 * <p>{@link FuzzifierImpl} is the default implementation.
 */
public interface Fuzzifier {

    /**
     * Fuzzifies a crisp value against every term of one variable.
     *
     * @return term name to degree of membership in that term
     */
    Map<String, Double> fuzzify(LinguisticVariable variable, double value);

    /**
     * Fuzzifies one crisp value per variable.
     *
     * @param variables the variables to fuzzify
     * @param values    variable name to crisp value, must contain every variable
     * @return variable name to (term name to degree)
     */
    Map<String, Map<String, Double>> fuzzify(
        List<LinguisticVariable> variables,
        Map<String, Double> values);
}
