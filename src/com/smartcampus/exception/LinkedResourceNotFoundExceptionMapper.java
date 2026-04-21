package com.smartcampus.exception;

import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.2 — Exception Mapper for 422 Unprocessable Entity
 *
 * Scenario: POST /api/v1/sensors with a roomId that does not exist.
 *
 * WHY 422 and not 404?
 *   - 404 means the URL you requested was not found on the server.
 *     The URL /api/v1/sensors is perfectly valid — the endpoint exists.
 *   - The problem is INSIDE the JSON body: the roomId field points to a
 *     room that doesn't exist. The JSON is syntactically correct, but
 *     semantically invalid (the reference is broken).
 *   - 422 Unprocessable Entity was designed exactly for this: "I understood
 *     your request and your JSON is valid, but the content makes no sense
 *     because a dependency is missing."
 *   - It gives developers a precise, actionable signal: fix the value
 *     inside the body, not the URL.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ApiError error = new ApiError(
                422,
                "Unprocessable Entity",
                "The value '" + ex.getValue() + "' provided for field '"
                + ex.getField() + "' does not reference an existing resource. "
                + "Please check the value and try again."
        );
        return Response
                .status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
