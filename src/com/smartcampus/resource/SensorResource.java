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

@Path("/api/v1/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty())
            return error(400, "Bad Request", "Field 'id' is required.");
        if (sensor.getType() == null || sensor.getType().trim().isEmpty())
            return error(400, "Bad Request", "Field 'type' is required.");
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty())
            return error(400, "Bad Request", "Field 'roomId' is required.");
        if (store.sensorExists(sensor.getId()))
            return error(409, "Conflict", "Sensor '" + sensor.getId() + "' already exists.");

        Room room = store.getRoom(sensor.getRoomId());
        if (room == null)
            throw new LinkedResourceNotFoundException("roomId", sensor.getRoomId());

        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty())
            sensor.setStatus("ACTIVE");

        store.saveSensor(sensor);
        room.getSensorIds().add(sensor.getId());

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null)
            return error(404, "Not Found", "Sensor '" + sensorId + "' does not exist.");
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null)
            return error(404, "Not Found", "Sensor '" + sensorId + "' does not exist.");
        Room room = store.getRoom(sensor.getRoomId());
        if (room != null) room.getSensorIds().remove(sensorId);
        store.deleteSensor(sensorId);
        return Response.noContent().build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        if (!store.sensorExists(sensorId)) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 404);
            body.put("error", "Not Found");
            body.put("message", "Sensor '" + sensorId + "' does not exist.");
            throw new WebApplicationException(Response.status(404).entity(body).build());
        }
        return new SensorReadingResource(sensorId);
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
