package com.smartcampus.exception;

import com.smartcampus.model.ApiError;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ApiError error = new ApiError(409, "Conflict",
                "Room '" + ex.getRoomId() + "' cannot be deleted — it still has "
                + ex.getSensorCount() + " sensor(s) assigned. "
                + "Please delete or reassign all sensors first.");
        return Response.status(409).type(MediaType.APPLICATION_JSON).entity(error).build();
    }
}
