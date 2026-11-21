package com.ayyoub.fuzzycoach.fuzzyengine.fuzzifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A variable with a universe of discourse {@code [min, max]} and a set of
 * linguistic terms (for example {@code low}, {@code medium}, {@code high}),
 * each backed by a membership function.
 *
 * <p>Terms keep their declaration order. Use {@link #named(String)} for a
 * fluent definition:
 *
 * <pre>{@code
 * LinguisticVariable motivation = LinguisticVariable
 *     .named("motivation")
 *     .range(0, 10)
 *     .term("low", new Triangle(0, 0, 5))
 *     .term("high", new Triangle(5, 10, 10))
 *     .build();
 * }</pre>
 */
public record LinguisticVariable(
        String name, double min, double max, Map<String, MembershipFunction> terms) {

    public LinguisticVariable {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Linguistic variable needs a name");
        }

        if (!(min < max)) {
            throw new IllegalArgumentException(
                    "Variable '" + name + "' requires min < max, got min=" + min + ", max=" + max);
        }

        if (terms == null || terms.isEmpty()) {
            throw new IllegalArgumentException("Variable '" + name + "' needs at least one term");
        }

        terms.forEach((termName, membership) -> {
            if (termName == null || termName.isBlank()) {
                throw new IllegalArgumentException(
                        "Variable '" + name + "' has a term without a name");
            }
            if (membership == null) {
                throw new IllegalArgumentException("Term '" + termName + "' of variable '" + name
                        + "' has no membership function");
            }
        });

        terms = Collections.unmodifiableMap(new LinkedHashMap<>(terms));
    }

    /**
     * Starts a fluent definition of a variable.
     */
    public static Builder named(String name) {
        return new Builder(name);
    }

    /**
     * Builds a {@link LinguisticVariable} term by term.
     */
    public static final class Builder {

        private final String name;
        private final Map<String, MembershipFunction> terms = new LinkedHashMap<>();
        private double min;
        private double max;
        private boolean rangeSet;

        private Builder(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Linguistic variable needs a name");
            }

            this.name = name;
        }

        public Builder range(double min, double max) {
            this.min = min;
            this.max = max;
            this.rangeSet = true;

            return this;
        }

        public Builder term(String termName, MembershipFunction membership) {
            if (termName == null || termName.isBlank()) {
                throw new IllegalArgumentException("Variable '" + name + "' needs a term name");
            }
            if (membership == null) {
                throw new IllegalArgumentException("Term '" + termName + "' of variable '" + name
                        + "' has no membership function");
            }
            if (terms.putIfAbsent(termName, membership) != null) {
                throw new IllegalArgumentException(
                        "Variable '" + name + "' declares term '" + termName + "' twice");
            }

            return this;
        }

        /**
         * @throws IllegalArgumentException when the range or the terms are missing
         */
        public LinguisticVariable build() {
            if (!rangeSet) {
                throw new IllegalArgumentException(
                        "Variable '" + name + "' needs a range, call range(min, max)");
            }

            return new LinguisticVariable(name, min, max, terms);
        }
    }
}
