package com.ayyoub.fuzzycoach.fuzzylogic.operator;

/**
 * Triangular norm used to combine conditions with AND.
 */
@FunctionalInterface
public interface TNorm {

    /**
     * Applies the norm to two degrees.
     *
     * @param a first degree
     * @param b second degree
     * @return combined degree
     */
    double apply(double a, double b);
}
