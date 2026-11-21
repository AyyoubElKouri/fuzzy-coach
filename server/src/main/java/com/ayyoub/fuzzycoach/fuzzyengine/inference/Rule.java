package com.ayyoub.fuzzycoach.fuzzyengine.inference;

import java.util.Objects;

/**
 * An if-then fuzzy rule, parsed once from text such as
 * {@code "IF motivation is high AND stress is low THEN intensity is medium"}.
 *
 * <p>A single condition needs no operator:
 * {@code "IF motivation is low THEN intensity is low"}.
 * {@code AND} binds tighter than {@code OR}, and parentheses override it.
 */
public final class Rule {

    private final Condition condition;
    private final Conclusion conclusion;

    /**
     * @throws RuleSyntaxException when the rule text is not valid
     */
    public Rule(String text) {
        RuleParser.ParsedRule parsed = RuleParser.parse(text);

        this.condition = parsed.condition();
        this.conclusion = parsed.conclusion();
    }

    /**
     * @return the premise of the rule, which may be a tree of conditions
     */
    public Condition condition() {
        return condition;
    }

    /**
     * @return the single conclusion of the rule
     */
    public Conclusion conclusion() {
        return conclusion;
    }

    @Override
    public String toString() {
        return "IF " + condition + " THEN " + conclusion;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Rule rule
                && condition.equals(rule.condition)
                && conclusion.equals(rule.conclusion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, conclusion);
    }
}
