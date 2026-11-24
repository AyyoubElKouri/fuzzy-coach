package com.ayyoub.fuzzycoach.fuzzylogic.operator;

/**
 * Standard complement: one minus the degree.
 */
public final class Complement {

    /**
     * Complements a degree.
     *
     * @param degree degree between {@code 0} and {@code 1}
     * @return {@code 1 - degree}
     */
    public double apply(double degree) {
        return 1.0 - degree;
    }
}
