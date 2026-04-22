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

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getAllReadings() {
        return Response.ok(store.getReadings(sensorId)).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) ||
            "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }
        if (reading.getId() == null || reading.getId().trim().isEmpty())
            reading.setId(UUID.randomUUID().toString());
        if (reading.getTimestamp() == 0)
            reading.setTimestamp(System.currentTimeMillis());

        store.addReading(sensorId, reading);
        return Response.created(
                URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId()))
                .entity(reading).build();
    }

    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> history = store.getReadings(sensorId);
        for (SensorReading r : history) {
            if (r.getId().equals(readingId)) return Response.ok(r).build();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", "Reading '" + readingId + "' not found.");
        return Response.status(404).entity(body).build();
    }
}
