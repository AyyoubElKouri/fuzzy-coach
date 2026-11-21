package com.ayyoub.fuzzycoach.fuzzyengine.membership;

/**
 * Triangular membership function defined by its left foot {@code a}, peak {@code b}
 * and right foot {@code c}.
 *
 * <p>Shoulder shapes are allowed, meaning {@code a == b} (left shoulder) or
 * {@code b == c} (right shoulder). The only requirement is {@code a <= b <= c}
 * and {@code a < c} so that the support is never empty.
 */
public record Triangle(double a, double b, double c) implements MembershipFunction {

    public Triangle {
        if (!(a <= b && b <= c)) {
            throw new IllegalArgumentException(
                    "Triangle requires a <= b <= c, got a=" + a + ", b=" + b + ", c=" + c);
        }
        if (a == c) {
            throw new IllegalArgumentException("Triangle requires a < c, got a=c=" + a);
        }
    }

    @Override
    public double degree(double x) {
        if (x <= a || x >= c) {
            return 0.0;
        }
        if (x == b) {
            return 1.0;
        }
        if (x < b) {
            return (x - a) / (b - a);
        }
        return (c - x) / (c - b);
    }
}
