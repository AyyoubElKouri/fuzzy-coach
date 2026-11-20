package com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier;

import com.ayyoub.fuzzycoach.fuzzyengine.core.FuzzyValue;

/**
 * Public API of the defuzzifier module: turns a fuzzy value into one crisp
 * number.
 *
 * <p>The only implementation for now is {@link CentroidDefuzzifier}.
 */
public interface Defuzzifier {

    /**
     * @return a crisp value inside the variable's universe
     */
    double defuzzify(FuzzyValue value);
}
