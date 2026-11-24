package com.ayyoub.fuzzycoach.fuzzylogic.defuzzification;

import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

/**
 * Converts the aggregated degrees of an output variable into a crisp value.
 */
@FunctionalInterface
public interface Defuzzifier {

    /**
     * Defuzzifies the given degrees over the range of the variable.
     *
     * @param variable output variable with the terms to evaluate
     * @param degrees aggregated activation degree of each term
     * @return the crisp value
     */
    double defuzzify(LinguisticVariable variable, FuzzySet degrees);
}
