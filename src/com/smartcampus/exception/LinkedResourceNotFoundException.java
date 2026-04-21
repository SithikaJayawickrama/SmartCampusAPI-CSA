package com.smartcampus.exception;

/**
 * Thrown when a sensor is registered with a roomId that does not exist.
 * Caught by LinkedResourceNotFoundExceptionMapper → 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String field;
    private final String value;

    public LinkedResourceNotFoundException(String field, String value) {
        super("The value '" + value + "' for field '" + field + "' references a resource that does not exist.");
        this.field = field;
        this.value = value;
    }

    public String getField() { return field; }
    public String getValue() { return value; }
}
