package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 3 — Sensor Management
 *
 * Handles all operations on the /api/v1/sensors collection.
 *
 * Endpoints:
 *   GET    /api/v1/sensors           → list all sensors (optional ?type= filter)
 *   POST   /api/v1/sensors           → register a new sensor
 *   GET    /api/v1/sensors/{id}      → get one sensor by ID
 *   *      /api/v1/sensors/{id}/readings → delegated to SensorReadingResource
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /sensors  or  GET /sensors?type=CO2 ──────────────────────────────
    /**
     * Lists all sensors. If the optional 'type' query parameter is provided,
     * only sensors of that type are returned.
     *
     * WHY @QueryParam and not a path like /sensors/type/CO2?
     * - The collection URL stays clean: /sensors always means "the sensor collection"
     * - The filter is optional — /sensors with no parameter still works
     * - Multiple filters compose naturally: ?type=CO2&status=ACTIVE
     * - Path parameters model resource identity, query params model filters/views
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>();

        for (Sensor s : store.getAllSensors()) {
            if (type == null || type.trim().isEmpty()
                    || s.getType().equalsIgnoreCase(type.trim())) {
                result.add(s);
            }
        }

        return Response.ok(result).build();
    }

    // ── POST /sensors ─────────────────────────────────────────────────────────
    /**
     * Registers a new sensor.
     *
     * @Consumes(APPLICATION_JSON) means JAX-RS will ONLY accept requests that
     * have the header "Content-Type: application/json". If a client sends
     * "Content-Type: text/plain" or "Content-Type: application/xml", JAX-RS
     * automatically rejects it with HTTP 415 Unsupported Media Type — before
     * this method body even runs.
     *
     * Business rule: the roomId in the request body MUST reference an existing
     * room. If not, we throw LinkedResourceNotFoundException → 422 response.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Validate required fields
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return errorResponse(400, "Bad Request", "Field 'id' is required.");
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return errorResponse(400, "Bad Request", "Field 'type' is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return errorResponse(400, "Bad Request", "Field 'roomId' is required.");
        }

        // No duplicate sensor IDs
        if (store.sensorExists(sensor.getId())) {
            return errorResponse(409, "Conflict",
                    "A sensor with id '" + sensor.getId() + "' already exists.");
        }

        // FOREIGN KEY VALIDATION: does the referenced room actually exist?
        Room room = store.getRoom(sensor.getRoomId());
        if (room == null) {
            // 422 Unprocessable Entity: the JSON is valid but semantically wrong —
            // the roomId points to a room that doesn't exist.
            throw new LinkedResourceNotFoundException("roomId", sensor.getRoomId());
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.saveSensor(sensor);

        // Add this sensor's ID to its room's sensor list
        room.getSensorIds().add(sensor.getId());

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // ── GET /sensors/{sensorId} ───────────────────────────────────────────────
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);

        if (sensor == null) {
            return errorResponse(404, "Not Found",
                    "Sensor '" + sensorId + "' does not exist.");
        }

        return Response.ok(sensor).build();
    }

    // ── DELETE /sensors/{sensorId} ────────────────────────────────────────────
    /**
     * Deletes a sensor and removes it from its room's sensor list.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);

        if (sensor == null) {
            return errorResponse(404, "Not Found",
                    "Sensor '" + sensorId + "' does not exist.");
        }

        // Remove from parent room's sensor list so the room can be deleted later
        Room room = store.getRoom(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        store.deleteSensor(sensorId);
        return Response.noContent().build();
    }

    // ── Sub-resource locator: delegates /sensors/{id}/readings ───────────────
    /**
     * Part 4 — Sub-Resource Locator Pattern
     *
     * This method does NOT handle an HTTP request itself. Instead, it hands off
     * control to SensorReadingResource for any URL under /sensors/{id}/readings.
     *
     * JAX-RS sees "/{sensorId}/readings" and calls this method. We return a new
     * instance of SensorReadingResource, and JAX-RS then asks THAT class to handle
     * the rest of the URL and the actual HTTP method (GET, POST, etc.).
     *
     * WHY is this better than putting everything in one big class?
     * - Each class has one job (separation of concerns)
     * - SensorReadingResource can be tested on its own
     * - Adding new sub-resources (/sensors/{id}/alerts) is just one new class
     * - RoomResource stays readable — not hundreds of lines long
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        // Validate sensor exists before delegating
        if (!store.sensorExists(sensorId)) {
            throw new javax.ws.rs.WebApplicationException(errorResponse(404, "Not Found", "Sensor '" + sensorId + "' does not exist."));
        }
        return new SensorReadingResource(sensorId);
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
