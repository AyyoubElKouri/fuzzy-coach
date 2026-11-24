package com.ayyoub.fuzzycoach.fuzzylogic.exception;

/**
 * Raised when an engine configuration is missing data, uses an unknown option, or cannot be read.
 */
public final class ConfigurationException extends FuzzyException {

    /**
     * Creates an exception with a message.
     *
     * @param message description of the failure
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and a cause.
     *
     * @param message description of the failure
     * @param cause underlying failure
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
