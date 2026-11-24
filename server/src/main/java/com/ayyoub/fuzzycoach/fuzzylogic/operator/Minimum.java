package com.ayyoub.fuzzycoach.fuzzylogic.operator;

/**
 * Minimum t-norm: the smallest of the two degrees.
 */
public final class Minimum implements TNorm {

    @Override
    public double apply(double a, double b) {
        return Math.min(a, b);
    }
}
