package com.ayyoub.fuzzycoach.fuzzylogic.membership;

/**
 * Triangular membership function.
 *
 * <p>The degree rises from {@code a} to {@code b} and falls from {@code b} to {@code c}.
 * Outside the interval {@code [a, c]} the degree is zero.
 */
public final class Triangular implements MembershipFunction {

    private final double a;
    private final double b;
    private final double c;

    /**
     * Creates a triangular membership function.
     *
     * @param a left vertex, less than or equal to {@code b}
     * @param b peak, between {@code a} and {@code c}
     * @param c right vertex, greater than or equal to {@code b}
     */
    public Triangular(double a, double b, double c) {
        if (!(a <= b && b <= c)) {
            throw new IllegalArgumentException("Vertices must satisfy a <= b <= c");
        }

        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public double membership(double value) {
        if (value <= a || value >= c) {
            return 0.0;
        }

        if (value == b) {
            return 1.0;
        }

        if (value < b) {
            return (value - a) / (b - a);
        }

        return (c - value) / (c - b);
    }
}
