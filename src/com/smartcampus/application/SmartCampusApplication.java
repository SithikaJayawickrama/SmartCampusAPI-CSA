package com.smartcampus.application;

import javax.ws.rs.core.Application;

/**
 * JAX-RS Application entry point.
 *
 * Note: @ApplicationPath is a JAX-RS 2.0 annotation and is not available
 * in Jersey 1.x (JAX-RS 1.1). The servlet base path "/api/v1" is configured
 * directly in Main.java via the Jetty ServletContextHandler.
 *
 * Example: @Path("/rooms") in RoomResource becomes /api/v1/rooms
 *
 * This class is retained for reference only. All resource registration
 * is handled manually in Main.java using DefaultResourceConfig.
 */
public class SmartCampusApplication extends Application {
    // Empty — resources are manually registered in Main.java
}
