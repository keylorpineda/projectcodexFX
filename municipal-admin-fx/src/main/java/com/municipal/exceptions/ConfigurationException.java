package com.municipal.exceptions;

/**
 * Thrown when the application configuration cannot be loaded or is invalid.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
