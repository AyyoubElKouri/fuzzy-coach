package com.ayyoub.fuzzycoach.fuzzyengine.defuzzifier;

/**
 * Sampling helpers shared by the discrete defuzzifiers.
 */
final class Universe {

    private Universe() {}

    /**
     * Evenly spaced sample points, placed at the middle of each interval.
     * Midpoints avoid biasing the result towards the edges of the universe.
     */
    static double[] grid(double min, double max, int samples) {
        if (samples <= 0) {
            throw new IllegalArgumentException("Samples must be positive, got " + samples);
        }

        double step = (max - min) / samples;
        double[] points = new double[samples];
        for (int i = 0; i < samples; i++) {
            points[i] = min + (i + 0.5) * step;
        }
        return points;
    }

    static double center(double min, double max) {
        return (min + max) / 2.0;
    }
}
