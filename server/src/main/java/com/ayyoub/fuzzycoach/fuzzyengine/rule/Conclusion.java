package com.ayyoub.fuzzycoach.fuzzyengine.rule;

/**
 * The conclusion of a rule: {@code variableName is termName}.
 *
 * <p>A conclusion names an output variable. It is a rule target, not a
 * condition, so it stays outside the {@link Condition} hierarchy.
 */
public record Conclusion(String variableName, String termName) {

    public Conclusion {
        if (variableName == null || variableName.isBlank()) {
            throw new IllegalArgumentException("A conclusion needs a variable name");
        }
        if (termName == null || termName.isBlank()) {
            throw new IllegalArgumentException("A conclusion needs a term name");
        }
    }

    @Override
    public String toString() {
        return variableName + " is " + termName;
    }
}
