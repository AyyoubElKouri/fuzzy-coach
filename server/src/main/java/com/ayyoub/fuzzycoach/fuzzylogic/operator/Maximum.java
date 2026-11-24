package com.ayyoub.fuzzycoach.fuzzylogic.operator;

/**
 * Maximum s-norm: the largest of the two degrees.
 */
public final class Maximum implements SNorm {

    @Override
    public double apply(double a, double b) {
        return Math.max(a, b);
    }
}
