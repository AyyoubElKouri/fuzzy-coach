package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import com.ayyoub.fuzzycoach.fuzzyengine.rule.Rule;

/**
 * One rule evaluated during inference: the rule, and how strongly it fired.
 *
 * <p>{@code activation} is the degree of the rule premise, in {@code [0, 1]}.
 * A rule with activation {@code 0} did not fire.
 */
public record RuleActivation(Rule rule, double activation) {

    public RuleActivation {
        if (rule == null) {
            throw new IllegalArgumentException("A rule activation needs a rule");
        }
        if (!Double.isFinite(activation) || activation < 0.0 || activation > 1.0) {
            throw new IllegalArgumentException("Activation must be in [0, 1], got " + activation);
        }
    }
}
