package com.smartcampus.exception;

import com.smartcampus.model.ApiError;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ApiError error = new ApiError(422, "Unprocessable Entity",
                "The value '" + ex.getValue() + "' for field '" + ex.getField()
                + "' does not reference an existing resource. Please check and try again.");
        return Response.status(422).type(MediaType.APPLICATION_JSON).entity(error).build();
    }
}
