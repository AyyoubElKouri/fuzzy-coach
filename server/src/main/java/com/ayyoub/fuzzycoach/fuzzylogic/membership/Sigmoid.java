package com.ayyoub.fuzzycoach.fuzzylogic.membership;

/**
 * Sigmoid membership function.
 *
 * <p>The degree is one half at {@code midpoint} and moves towards one or zero on either
 * side, depending on the sign of {@code slope}.
 */
public final class Sigmoid implements MembershipFunction {

    private final double midpoint;
    private final double slope;

    /**
     * Creates a sigmoid membership function.
     *
     * @param midpoint value where the degree is one half
     * @param slope steepness and direction, must not be zero
     */
    public Sigmoid(double midpoint, double slope) {
        if (!(slope != 0)) {
            throw new IllegalArgumentException("Slope must not be zero");
        }

        this.midpoint = midpoint;
        this.slope = slope;
    }

    @Override
    public double membership(double value) {
        return 1.0 / (1.0 + Math.exp(-slope * (value - midpoint)));
    }
}
