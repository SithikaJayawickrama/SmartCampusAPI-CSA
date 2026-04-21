package com.smartcampus.exception;

import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.3 — Exception Mapper for 403 Forbidden
 *
 * Scenario: POST /api/v1/sensors/{id}/readings when the sensor status is MAINTENANCE.
 *
 * WHY 403 Forbidden?
 *   - The sensor resource exists (not 404).
 *   - The client is not unauthenticated (not 401).
 *   - The server understands the request completely.
 *   - BUT the current state of the sensor (MAINTENANCE) explicitly forbids
 *     the operation. The server is saying: "I know what you want to do,
 *     and I am deliberately refusing it based on business rules."
 *   - 403 is the correct code for a policy-based refusal.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ApiError error = new ApiError(
                403,
                "Forbidden",
                "Sensor '" + ex.getSensorId() + "' is currently in '"
                + ex.getStatus() + "' status and cannot accept new readings. "
                + "The sensor must be set back to ACTIVE before readings can be recorded."
        );
        return Response
                .status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
