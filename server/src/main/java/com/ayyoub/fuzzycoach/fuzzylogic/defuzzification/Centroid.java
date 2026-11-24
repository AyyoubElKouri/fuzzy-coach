package com.ayyoub.fuzzycoach.fuzzylogic.defuzzification;

import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

/**
 * Center of gravity of the aggregated fuzzy set.
 */
public final class Centroid implements Defuzzifier {

    private final int resolution;

    public Centroid() {
        this(Aggregate.DEFAULT_RESOLUTION);
    }

    /**
     * Creates a centroid defuzzifier.
     *
     * @param resolution number of samples used over the variable range
     */
    public Centroid(int resolution) {
        Aggregate.requireResolution(resolution);

        this.resolution = resolution;
    }

    @Override
    public double defuzzify(LinguisticVariable variable, FuzzySet degrees) {
        Aggregate.requireValid(variable, degrees);

        return Aggregate.centroid(
                variable, value -> Aggregate.membership(variable, degrees, value), resolution);
    }
}
