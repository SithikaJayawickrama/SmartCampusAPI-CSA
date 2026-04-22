package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/api/v1/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        return Response.ok(store.getAllRooms()).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty())
            return error(400, "Bad Request", "Field 'id' is required.");
        if (room.getName() == null || room.getName().trim().isEmpty())
            return error(400, "Bad Request", "Field 'name' is required.");
        if (store.roomExists(room.getId()))
            return error(409, "Conflict", "Room '" + room.getId() + "' already exists.");

        store.saveRoom(room);
        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null)
            return error(404, "Not Found", "Room '" + roomId + "' does not exist.");
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null)
            return error(404, "Not Found", "Room '" + roomId + "' does not exist.");
        if (!room.getSensorIds().isEmpty())
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }

    private Response error(int status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        return Response.status(status).entity(body).build();
    }
}
