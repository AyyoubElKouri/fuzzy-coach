package com.ayyoub.fuzzycoach.fuzzylogic.defuzzification;

import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

/**
 * Mean of the values where the aggregated fuzzy set reaches its maximum.
 */
public final class MeanOfMax implements Defuzzifier {

    private final int resolution;

    public MeanOfMax() {
        this(Aggregate.DEFAULT_RESOLUTION);
    }

    /**
     * Creates a mean of maximum defuzzifier.
     *
     * @param resolution number of samples used over the variable range
     */
    public MeanOfMax(int resolution) {
        Aggregate.requireResolution(resolution);

        this.resolution = resolution;
    }

    @Override
    public double defuzzify(LinguisticVariable variable, FuzzySet degrees) {
        Aggregate.requireValid(variable, degrees);

        double maximum = 0.0;

        for (int i = 0; i <= resolution; i++) {
            maximum = Math.max(maximum, membership(variable, degrees, i));
        }

        if (maximum == 0.0) {
            return Aggregate.midpoint(variable);
        }

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i <= resolution; i++) {
            if (membership(variable, degrees, i) >= maximum - Aggregate.EPSILON) {
                sum += Aggregate.valueAt(variable, resolution, i);
                count++;
            }
        }

        return sum / count;
    }

    private double membership(LinguisticVariable variable, FuzzySet degrees, int index) {
        return Aggregate.membership(
                variable, degrees, Aggregate.valueAt(variable, resolution, index));
    }
}
