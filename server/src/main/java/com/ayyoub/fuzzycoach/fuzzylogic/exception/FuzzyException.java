package com.ayyoub.fuzzycoach.fuzzylogic.exception;

/**
 * Base type for errors raised by the fuzzy logic engine.
 */
public class FuzzyException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message description of the failure
     */
    public FuzzyException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message description of the failure
     * @param cause underlying failure
     */
    public FuzzyException(String message, Throwable cause) {
        super(message, cause);
    }
}
