package com.ayyoub.fuzzycoach.fuzzylogic.operator;

/**
 * Triangular conorm used to combine conditions with OR.
 */
@FunctionalInterface
public interface SNorm {

    /**
     * Applies the norm to two degrees.
     *
     * @param a first degree
     * @param b second degree
     * @return combined degree
     */
    double apply(double a, double b);
}
