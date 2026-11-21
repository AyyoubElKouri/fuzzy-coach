package com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier;

import com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier.FuzzyValue;

/**
 * Center of gravity: the weighted average of {@code x} over the fuzzy set.
 *
 * <pre>
 *   x* = sum(x * mu(x)) / sum(mu(x))
 * </pre>
 *
 * <p>This is the default and the most common Mamdani defuzzifier. It is smooth
 * and reacts to the whole shape of the output, not just its peak.
 *
 * <p>When no rule contributes any membership, the centroid is undefined, so
 * the center of the universe is returned instead.
 */
public class CentroidDefuzzifier implements Defuzzifier {

    private static final int DEFAULT_SAMPLES = 1000;

    private final int samples;

    public CentroidDefuzzifier() {
        this(DEFAULT_SAMPLES);
    }

    public CentroidDefuzzifier(int samples) {
        if (samples <= 0) {
            throw new IllegalArgumentException("Samples must be positive, got " + samples);
        }

        this.samples = samples;
    }

    @Override
    public double defuzzify(FuzzyValue output) {
        double min = output.variable().min();
        double max = output.variable().max();

        double weightedSum = 0.0;
        double totalMembership = 0.0;
        for (double x : Universe.grid(min, max, samples)) {
            double membership = output.membership(x);
            weightedSum += x * membership;
            totalMembership += membership;
        }

        if (totalMembership == 0.0) {
            return Universe.center(min, max);
        }

        return weightedSum / totalMembership;
    }
}
