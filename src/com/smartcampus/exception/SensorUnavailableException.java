package com.smartcampus.exception;

/**
 * Thrown when a client posts a reading to a sensor that is in MAINTENANCE
 * or OFFLINE status and physically cannot accept new data.
 * Caught by SensorUnavailableExceptionMapper → 403 Forbidden.
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String status;

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' is in '" + status + "' state and cannot accept readings.");
        this.sensorId = sensorId;
        this.status   = status;
    }

    public String getSensorId() { return sensorId; }
    public String getStatus()   { return status; }
}
