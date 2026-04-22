package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String field;
    private final String value;
    public LinkedResourceNotFoundException(String field, String value) {
        super("Field '" + field + "' value '" + value + "' references a non-existent resource.");
        this.field = field; this.value = value;
    }
    public String getField() { return field; }
    public String getValue() { return value; }
}
