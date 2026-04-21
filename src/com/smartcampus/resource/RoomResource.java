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

/**
 * Part 2 — Room Management
 *
 * Handles all operations on the /api/v1/rooms collection.
 *
 * Endpoints:
 *   GET    /api/v1/rooms             → list all rooms
 *   POST   /api/v1/rooms             → create a new room
 *   GET    /api/v1/rooms/{roomId}    → get one room by ID
 *   DELETE /api/v1/rooms/{roomId}    → delete a room (blocked if it has sensors)
 *
 * IMPORTANT — JAX-RS Lifecycle:
 * A new instance of this class is created for EVERY request. That is why we
 * do NOT store rooms as a field here. We fetch them from the shared DataStore
 * singleton instead — the store lives for the whole server lifetime.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /rooms ────────────────────────────────────────────────────────────
    /**
     * Returns the full list of all rooms.
     *
     * Returning full objects (not just IDs) means clients can display a complete
     * room list in one request. The downside is a larger payload when there are
     * many rooms, but for a campus-scale system this is acceptable.
     */
    @GET
    public Response getAllRooms() {
        return Response.ok(store.getAllRooms()).build();
    }

    // ── POST /rooms ───────────────────────────────────────────────────────────
    /**
     * Creates a new room. Returns 201 Created with a Location header pointing
     * to the new room's URL — this is standard REST practice for POST.
     */
    @POST
    public Response createRoom(Room room) {
        // Validate required fields
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return errorResponse(400, "Bad Request", "Field 'id' is required.");
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return errorResponse(400, "Bad Request", "Field 'name' is required.");
        }

        // Prevent duplicate IDs
        if (store.roomExists(room.getId())) {
            return errorResponse(409, "Conflict",
                    "A room with id '" + room.getId() + "' already exists.");
        }

        store.saveRoom(room);

        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    // ── GET /rooms/{roomId} ───────────────────────────────────────────────────
    /**
     * Returns one specific room by its ID.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);

        if (room == null) {
            return errorResponse(404, "Not Found",
                    "Room '" + roomId + "' does not exist.");
        }

        return Response.ok(room).build();
    }

    // ── DELETE /rooms/{roomId} ────────────────────────────────────────────────
    /**
     * Deletes a room — but ONLY if it has no sensors assigned to it.
     * If the room still has sensors, we throw RoomNotEmptyException which
     * gets mapped to a 409 Conflict response by RoomNotEmptyExceptionMapper.
     *
     * IS DELETE IDEMPOTENT HERE?
     * Yes. Calling DELETE on the same room ID multiple times always leaves the
     * server in the same state (room doesn't exist). The first call returns 204,
     * subsequent calls return 404 — but the server state is identical each time.
     * REST idempotency is about server state, not response codes.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);

        if (room == null) {
            return errorResponse(404, "Not Found",
                    "Room '" + roomId + "' does not exist.");
        }

        // Safety check: cannot delete a room that still contains sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }

        store.deleteRoom(roomId);
        return Response.noContent().build(); // 204 No Content
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Response errorResponse(int status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",    status);
        body.put("error",     error);
        body.put("message",   message);
        body.put("timestamp", System.currentTimeMillis());
        return Response.status(status).entity(body).build();
    }
}
