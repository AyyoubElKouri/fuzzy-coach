package com.ayyoub.fuzzycoach.fuzzylogic.rule;

import com.ayyoub.fuzzycoach.fuzzylogic.variable.LinguisticVariable;
import com.ayyoub.fuzzycoach.fuzzylogic.variable.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The THEN part of a rule: one or more conclusions of the form variable is term.
 */
public final class Consequent {

    private final List<Conclusion> conclusions;

    /**
     * Creates a consequent with a single conclusion.
     *
     * @param variable output variable
     * @param term term assigned to the variable
     */
    public Consequent(LinguisticVariable variable, Term term) {
        this(List.of(new Conclusion(variable, term)));
    }

    private Consequent(List<Conclusion> conclusions) {
        this.conclusions = List.copyOf(conclusions);
    }

    /**
     * Adds another conclusion to this consequent.
     *
     * @param variable output variable
     * @param term term assigned to the variable
     * @return a new consequent with the added conclusion
     */
    public Consequent and(LinguisticVariable variable, Term term) {
        List<Conclusion> combined = new ArrayList<>(conclusions);
        combined.add(new Conclusion(variable, term));

        return new Consequent(combined);
    }

    public List<Conclusion> conclusions() {
        return conclusions;
    }

    /**
     * A single conclusion: a term assigned to an output variable.
     */
    public record Conclusion(LinguisticVariable variable, Term term) {

        public Conclusion {
            Objects.requireNonNull(variable, "Variable is required");
            Objects.requireNonNull(term, "Term is required");
            variable.term(term.name());
        }
    }
}
