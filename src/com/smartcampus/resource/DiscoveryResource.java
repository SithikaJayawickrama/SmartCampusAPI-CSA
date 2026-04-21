package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1.2 — Discovery Endpoint
 *
 * GET /api/v1
 *
 * Returns metadata about the API: version, contact info, and links to all
 * available resource collections (this is called HATEOAS — the idea that
 * the API tells you where to go next, like a website with navigation links).
 *
 * Without HATEOAS, a client developer has to read documentation to find
 * "/api/v1/rooms". With HATEOAS they just call "/" and the response tells them.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0.0");
        response.put("description", "A RESTful API to manage campus rooms and IoT sensors");
        response.put("module",      "5COSC022W - Client-Server Architectures");
        response.put("contact", buildMap(
                "name",  "Campus Facilities Team",
                "email", "facilities@university.ac.uk"
        ));

        // HATEOAS links: tell the client what they can explore from here
        response.put("_links", buildMap(
                "self",    "/api/v1",
                "rooms",   "/api/v1/rooms",
                "sensors", "/api/v1/sensors"
        ));

        return Response.ok(response).build();
    }

    // Small helper to build a map inline without verbose put() calls
    private Map<String, String> buildMap(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
