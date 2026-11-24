package com.ayyoub.fuzzycoach.fuzzylogic.defuzzification;

import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * Sampling helpers shared by the defuzzifiers.
 */
final class Aggregate {

    static final int DEFAULT_RESOLUTION = 100;
    static final double EPSILON = 1e-9;

    private Aggregate() {}

    static void requireResolution(int resolution) {
        if (resolution <= 0) {
            throw new IllegalArgumentException("Resolution must be greater than zero");
        }
    }

    static void requireValid(LinguisticVariable variable, FuzzySet degrees) {
        Objects.requireNonNull(variable, "Variable is required");
        Objects.requireNonNull(degrees, "Degrees are required");
    }

    static double membership(LinguisticVariable variable, FuzzySet degrees, double value) {
        double result = 0.0;

        for (Map.Entry<String, Double> entry : degrees.degrees().entrySet()) {
            Term term = variable.term(entry.getKey());
            double clipped = Math.min(entry.getValue(), term.membership(value));
            result = Math.max(result, clipped);
        }

        return result;
    }

    static double valueAt(LinguisticVariable variable, int resolution, int index) {
        double step = (variable.maximum() - variable.minimum()) / resolution;

        return variable.minimum() + index * step;
    }

    static double centroid(
            LinguisticVariable variable, DoubleUnaryOperator membership, int resolution) {
        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i <= resolution; i++) {
            double value = valueAt(variable, resolution, i);
            double degree = membership.applyAsDouble(value);
            numerator += value * degree;
            denominator += degree;
        }

        if (denominator == 0.0) {
            return midpoint(variable);
        }

        return numerator / denominator;
    }

    static double midpoint(LinguisticVariable variable) {
        return (variable.minimum() + variable.maximum()) / 2.0;
    }
}
