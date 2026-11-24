package com.ayyoub.fuzzycoach.fuzzylogic.defuzzification;

import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.Map;

/**
 * Weighted average of the term centers, using each term activation as weight.
 */
public final class WeightedAverage implements Defuzzifier {

    private final int resolution;

    public WeightedAverage() {
        this(Aggregate.DEFAULT_RESOLUTION);
    }

    /**
     * Creates a weighted average defuzzifier.
     *
     * @param resolution number of samples used over the variable range
     */
    public WeightedAverage(int resolution) {
        Aggregate.requireResolution(resolution);

        this.resolution = resolution;
    }

    @Override
    public double defuzzify(LinguisticVariable variable, FuzzySet degrees) {
        Aggregate.requireValid(variable, degrees);

        double numerator = 0.0;
        double denominator = 0.0;

        for (Map.Entry<String, Double> entry : degrees.degrees().entrySet()) {
            double activation = entry.getValue();

            if (activation <= 0.0) {
                continue;
            }

            Term term = variable.term(entry.getKey());
            double center = Aggregate.centroid(variable, term::membership, resolution);
            numerator += activation * center;
            denominator += activation;
        }

        if (denominator == 0.0) {
            return Aggregate.midpoint(variable);
        }

        return numerator / denominator;
    }
}
