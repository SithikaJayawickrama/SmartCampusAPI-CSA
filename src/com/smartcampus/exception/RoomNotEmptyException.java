package com.smartcampus.exception;

/**
 * Thrown when a client tries to DELETE a room that still has sensors in it.
 * This is caught by RoomNotEmptyExceptionMapper and turned into a 409 Conflict response.
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;
    private final int sensorCount;

    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Room '" + roomId + "' still has " + sensorCount + " sensor(s) assigned to it.");
        this.roomId      = roomId;
        this.sensorCount = sensorCount;
    }

    public String getRoomId()    { return roomId; }
    public int getSensorCount()  { return sensorCount; }
}
