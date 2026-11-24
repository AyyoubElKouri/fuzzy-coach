package com.ayyoub.fuzzycoach.fuzzylogic.membership;

/**
 * Assigns a degree of membership in {@code [0, 1]} to a crisp value.
 */
@FunctionalInterface
public interface MembershipFunction {

    /**
     * Returns the degree of membership of the given value.
     *
     * @param value crisp input value
     * @return degree of membership between {@code 0} and {@code 1}
     */
    double membership(double value);
}
