package com.ayyoub.fuzzycoach.fuzzylogic.membership;

/**
 * Trapezoidal membership function.
 *
 * <p>The degree rises from {@code a} to {@code b}, stays at one between {@code b} and
 * {@code c}, and falls from {@code c} to {@code d}. Outside {@code [a, d]} the degree is zero.
 */
public final class Trapezoidal implements MembershipFunction {

    private final double a;
    private final double b;
    private final double c;
    private final double d;

    /**
     * Creates a trapezoidal membership function.
     *
     * @param a left vertex, less than or equal to {@code b}
     * @param b left shoulder, between {@code a} and {@code c}
     * @param c right shoulder, between {@code b} and {@code d}
     * @param d right vertex, greater than or equal to {@code c}
     */
    public Trapezoidal(double a, double b, double c, double d) {
        if (!(a <= b && b <= c && c <= d)) {
            throw new IllegalArgumentException("Vertices must satisfy a <= b <= c <= d");
        }

        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Override
    public double membership(double value) {
        if (a < value && value < b) {
            return (value - a) / (b - a);
        }

        if (b <= value && value <= c) {
            return 1.0;
        }

        if (c < value && value < d) {
            return (d - value) / (d - c);
        }

        return 0.0;
    }
}
