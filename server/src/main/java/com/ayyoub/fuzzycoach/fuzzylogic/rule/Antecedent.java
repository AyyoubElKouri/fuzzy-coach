package com.ayyoub.fuzzycoach.fuzzylogic.rule;

import com.ayyoub.fuzzycoach.fuzzylogic.operator.Complement;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.SNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.operator.TNorm;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.FuzzySet;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.Objects;
import java.util.function.Function;

/**
 * The IF part of a rule: propositions about variables and terms, combined with AND, OR, and NOT.
 */
public final class Antecedent {

    private final Node root;

    private Antecedent(Node root) {
        this.root = root;
    }

    /**
     * Creates an antecedent with a single proposition.
     *
     * @param variable variable to test
     * @param term term the variable must belong to
     * @return the antecedent
     */
    public static Antecedent of(LinguisticVariable variable, Term term) {
        Objects.requireNonNull(variable, "Variable is required");
        Objects.requireNonNull(term, "Term is required");
        variable.term(term.name());

        return new Antecedent(new Proposition(variable, term));
    }

    /**
     * Combines two antecedents with AND.
     *
     * @param left first antecedent
     * @param right second antecedent
     * @return the combination
     */
    public static Antecedent and(Antecedent left, Antecedent right) {
        return new Antecedent(new Combination(Operator.AND, node(left), node(right)));
    }

    /**
     * Combines two antecedents with OR.
     *
     * @param left first antecedent
     * @param right second antecedent
     * @return the combination
     */
    public static Antecedent or(Antecedent left, Antecedent right) {
        return new Antecedent(new Combination(Operator.OR, node(left), node(right)));
    }

    /**
     * Negates an antecedent.
     *
     * @param antecedent antecedent to negate
     * @return the negation
     */
    public static Antecedent not(Antecedent antecedent) {
        return new Antecedent(new Negation(node(antecedent)));
    }

    /**
     * Evaluates the antecedent against the fuzzified inputs.
     *
     * @param inputs fuzzified values indexed by variable name
     * @param and operator used for AND
     * @param or operator used for OR
     * @param complement operator used for NOT
     * @return the resulting degree
     */
    public double degree(
            Function<String, FuzzySet> inputs, TNorm and, SNorm or, Complement complement) {
        Objects.requireNonNull(inputs, "Inputs are required");
        Objects.requireNonNull(and, "And operator is required");
        Objects.requireNonNull(or, "Or operator is required");
        Objects.requireNonNull(complement, "Complement is required");

        return root.degree(inputs, and, or, complement);
    }

    private static Node node(Antecedent antecedent) {
        return Objects.requireNonNull(antecedent, "Antecedent is required").root;
    }

    private enum Operator {
        AND,
        OR
    }

    private sealed interface Node permits Proposition, Combination, Negation {
        double degree(
                Function<String, FuzzySet> inputs, TNorm and, SNorm or, Complement complement);
    }

    private record Proposition(LinguisticVariable variable, Term term) implements Node {

        @Override
        public double degree(
                Function<String, FuzzySet> inputs, TNorm and, SNorm or, Complement complement) {
            FuzzySet fuzzySet = inputs.apply(variable.name());

            if (fuzzySet == null) {
                throw new IllegalArgumentException("Missing input: " + variable.name());
            }

            return fuzzySet.degree(term.name());
        }
    }

    private record Combination(Operator operator, Node left, Node right) implements Node {

        @Override
        public double degree(
                Function<String, FuzzySet> inputs, TNorm and, SNorm or, Complement complement) {
            double leftDegree = left.degree(inputs, and, or, complement);
            double rightDegree = right.degree(inputs, and, or, complement);

            return operator == Operator.AND
                    ? and.apply(leftDegree, rightDegree)
                    : or.apply(leftDegree, rightDegree);
        }
    }

    private record Negation(Node operand) implements Node {

        @Override
        public double degree(
                Function<String, FuzzySet> inputs, TNorm and, SNorm or, Complement complement) {
            return complement.apply(operand.degree(inputs, and, or, complement));
        }
    }
}
