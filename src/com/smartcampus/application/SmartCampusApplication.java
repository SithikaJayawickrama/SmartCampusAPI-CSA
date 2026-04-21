package com.smartcampus.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 *
 * @ApplicationPath("/api/v1") sets the base URL for the entire API.
 * Every @Path annotation in resource classes is relative to this base.
 *
 * Example: @Path("/rooms") in RoomResource becomes /api/v1/rooms
 *
 * By leaving the class body empty, Jersey automatically scans the entire
 * classpath for @Path and @Provider annotated classes and registers them.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Empty — Jersey auto-discovers all resource and provider classes
}
