package com.ayyoub.fuzzycoach.fuzzylogic.membership;

/**
 * Gaussian membership function.
 *
 * <p>The degree reaches one at {@code mean} and decays with distance controlled by
 * {@code sigma}.
 */
public final class Gaussian implements MembershipFunction {

    private final double mean;
    private final double sigma;

    /**
     * Creates a Gaussian membership function.
     *
     * @param mean center of the curve
     * @param sigma standard deviation, greater than zero
     */
    public Gaussian(double mean, double sigma) {
        if (!(sigma > 0)) {
            throw new IllegalArgumentException("Sigma must be greater than zero");
        }

        this.mean = mean;
        this.sigma = sigma;
    }

    @Override
    public double membership(double value) {
        double distance = (value - mean) / sigma;

        return Math.exp(-0.5 * distance * distance);
    }
}
