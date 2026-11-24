package com.ayyoub.fuzzycoach.fuzzylogic.operator;

/**
 * Product t-norm: the multiplication of the two degrees.
 */
public final class Product implements TNorm {

    @Override
    public double apply(double a, double b) {
        return a * b;
    }
}
