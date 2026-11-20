package com.ayyoub.fuzzycoach.fuzzyengine.core;

import java.util.Map;

/**
 * A variable with a universe of discourse {@code [min, max]} and a set of
 * linguistic terms (for example {@code low}, {@code medium}, {@code high}),
 * each backed by a membership function.
 */
public record LinguisticVariable(
    String name,
    double min,
    double max,
    Map<String, MembershipFunction> terms) {

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
                throw new IllegalArgumentException(
                    "Term '" + termName + "' of variable '" + name
                        + "' has no membership function");
            }
        });

        terms = Map.copyOf(terms);
    }
}
