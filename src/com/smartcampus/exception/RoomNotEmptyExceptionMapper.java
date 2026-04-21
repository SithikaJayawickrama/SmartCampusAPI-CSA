package com.smartcampus.exception;

import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.1 — Exception Mapper for 409 Conflict
 *
 * JAX-RS automatically calls this when a RoomNotEmptyException is thrown anywhere.
 * The @Provider annotation tells Jersey to register this as an exception handler.
 *
 * Scenario: DELETE /api/v1/rooms/LIB-301 when LIB-301 still has sensors.
 * Response: HTTP 409 Conflict with a JSON explanation.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ApiError error = new ApiError(
                409,
                "Conflict",
                "Room '" + ex.getRoomId() + "' cannot be deleted because it still has "
                + ex.getSensorCount() + " sensor(s) assigned to it. "
                + "Please delete or reassign all sensors in this room first."
        );
        return Response
                .status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
