package com.ayyoub.fuzzycoach.fuzzylogic.variable;

import com.ayyoub.fuzzycoach.fuzzylogic.membership.MembershipFunction;

/**
 * A named membership function attached to a linguistic variable, such as {@code low}.
 */
public final class Term {

    private final String name;
    private final MembershipFunction membershipFunction;

    /**
     * Creates a term.
     *
     * @param name label of the term
     * @param membershipFunction shape that defines the term
     */
    public Term(String name, MembershipFunction membershipFunction) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Term name is required");
        }

        if (membershipFunction == null) {
            throw new IllegalArgumentException("Membership function is required");
        }

        this.name = name;
        this.membershipFunction = membershipFunction;
    }

    public String name() {
        return name;
    }

    public MembershipFunction membershipFunction() {
        return membershipFunction;
    }

    /**
     * Returns the degree of membership of the given value in this term.
     *
     * @param value crisp value
     * @return degree between {@code 0} and {@code 1}
     */
    public double membership(double value) {
        return membershipFunction.membership(value);
    }
}
