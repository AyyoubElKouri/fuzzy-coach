package com.ayyoub.fuzzycoach.fuzzylogic.exception;

/**
 * Raised when a rule refers to an unknown variable or term, or is structurally invalid.
 */
public final class InvalidRuleException extends FuzzyException {

    /**
     * Creates an exception with a message.
     *
     * @param message description of the failure
     */
    public InvalidRuleException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message description of the failure
     * @param cause underlying failure
     */
    public InvalidRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
