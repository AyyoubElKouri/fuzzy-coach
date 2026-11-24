package com.ayyoub.fuzzycoach.fuzzylogic.rule;

import com.ayyoub.fuzzycoach.fuzzylogic.operator.Complement;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.SNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.TNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Collection of rules that are evaluated together against the same inputs.
 */
public final class RuleBase {

    private final List<Rule> rules = new ArrayList<>();

    /**
     * Adds a rule to this base.
     *
     * @param rule rule to add
     */
    public void addRule(Rule rule) {
        Objects.requireNonNull(rule, "Rule is required");

        rules.add(rule);
    }

    /**
     * Evaluates every rule and stores its activation degree.
     *
     * @param inputs fuzzified values indexed by variable name
     * @param and operator used for AND
     * @param or operator used for OR
     * @param complement operator used for NOT
     */
    public void evaluate(
            Function<String, FuzzySet> inputs, TNorm and, SNorm or, Complement complement) {
        Objects.requireNonNull(inputs, "Inputs are required");
        Objects.requireNonNull(and, "And operator is required");
        Objects.requireNonNull(or, "Or operator is required");
        Objects.requireNonNull(complement, "Complement is required");

        for (Rule rule : rules) {
            double degree = rule.antecedent().degree(inputs, and, or, complement);
            rule.activate(degree);
        }
    }

    public List<Rule> rules() {
        return Collections.unmodifiableList(rules);
    }
}
