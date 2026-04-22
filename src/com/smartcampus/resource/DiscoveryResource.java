package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0.0");
        response.put("description", "A RESTful API to manage campus rooms and IoT sensors");
        response.put("module",      "5COSC022W - Client-Server Architectures");
        response.put("student",     "W2121304 - Alankarage Sithika Jayawickrama");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("_links", links);

        return Response.ok(response).build();
    }
}
