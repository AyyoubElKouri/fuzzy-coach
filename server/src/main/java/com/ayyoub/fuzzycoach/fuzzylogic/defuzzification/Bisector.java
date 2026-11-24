package com.ayyoub.fuzzycoach.fuzzylogic.defuzzification;

import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;

/**
 * Value that splits the area of the aggregated fuzzy set in two equal halves.
 */
public final class Bisector implements Defuzzifier {

    private final int resolution;

    public Bisector() {
        this(Aggregate.DEFAULT_RESOLUTION);
    }

    /**
     * Creates a bisector defuzzifier.
     *
     * @param resolution number of samples used over the variable range
     */
    public Bisector(int resolution) {
        Aggregate.requireResolution(resolution);

        this.resolution = resolution;
    }

    @Override
    public double defuzzify(LinguisticVariable variable, FuzzySet degrees) {
        Aggregate.requireValid(variable, degrees);

        double total = 0.0;
        for (int i = 0; i <= resolution; i++) {
            total += membership(variable, degrees, i);
        }

        if (total == 0.0) {
            return Aggregate.midpoint(variable);
        }

        double half = total / 2.0;
        double accumulated = 0.0;

        for (int i = 0; i <= resolution; i++) {
            accumulated += membership(variable, degrees, i);

            if (accumulated >= half) {
                return Aggregate.valueAt(variable, resolution, i);
            }
        }

        return variable.maximum();
    }

    private double membership(LinguisticVariable variable, FuzzySet degrees, int index) {
        return Aggregate.membership(
                variable, degrees, Aggregate.valueAt(variable, resolution, index));
    }
}
