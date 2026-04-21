package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Part 4 — Sensor Reading Sub-Resource
 *
 * This class is NOT annotated with @Path at the class level.
 * It is reached only via the sub-resource locator in SensorResource:
 *   SensorResource.getReadingsResource() → returns new SensorReadingResource(sensorId)
 *
 * Full URL pattern:  /api/v1/sensors/{sensorId}/readings
 *
 * Endpoints:
 *   GET  /api/v1/sensors/{sensorId}/readings          → get all readings (history)
 *   POST /api/v1/sensors/{sensorId}/readings          → add a new reading
 *   GET  /api/v1/sensors/{sensorId}/readings/{readId} → get one specific reading
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ── GET /sensors/{sensorId}/readings ─────────────────────────────────────
    /**
     * Returns the full reading history for this sensor.
     * Readings are stored in insertion order.
     */
    @GET
    public Response getAllReadings() {
        List<SensorReading> history = store.getReadings(sensorId);
        return Response.ok(history).build();
    }

    // ── POST /sensors/{sensorId}/readings ─────────────────────────────────────
    /**
     * Adds a new sensor reading.
     *
     * Business rule: if the sensor is in MAINTENANCE status, it is physically
     * disconnected from the network and cannot send readings. We throw
     * SensorUnavailableException → 403 Forbidden.
     *
     * Side-effect (Part 4.2 requirement):
     * After saving the reading, we also update the parent Sensor object's
     * currentValue field. This keeps the two data sources consistent —
     * GET /sensors/{id} will always show the most recent measurement.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);

        // State constraint: MAINTENANCE sensors cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // OFFLINE sensors also cannot accept readings
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // Auto-generate ID if not provided
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Auto-set timestamp if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save reading AND update sensor.currentValue (both happen inside DataStore)
        store.addReading(sensorId, reading);

        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();
    }

    // ── GET /sensors/{sensorId}/readings/{readingId} ──────────────────────────
    /**
     * Retrieves one specific reading by its ID.
     */
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> history = store.getReadings(sensorId);

        for (SensorReading r : history) {
            if (r.getId().equals(readingId)) {
                return Response.ok(r).build();
            }
        }

        return errorResponse(404, "Not Found",
                "Reading '" + readingId + "' not found for sensor '" + sensorId + "'.");
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
