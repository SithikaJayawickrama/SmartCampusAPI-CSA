package com.smartcampus.exception;

import com.smartcampus.model.ApiError;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ApiError error = new ApiError(403, "Forbidden",
                "Sensor '" + ex.getSensorId() + "' is currently in '"
                + ex.getStatus() + "' status and cannot accept new readings. "
                + "Set the sensor back to ACTIVE first.");
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON).entity(error).build();
    }
}
