package com.smartcampus.application;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Logger;

/**
 * Embedded Jetty 9 server entry point.
 *
 * Jersey 1's ServletContainer is registered as the JAX-RS dispatcher.
 * The "com.sun.jersey.config.property.packages" init param triggers
 * Jersey's classpath scanning so all @Path resources and @Provider
 * classes under "com.smartcampus" are discovered automatically —
 * mirroring the behaviour of @ApplicationPath in a full Java EE container.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Server server = new Server(PORT);

        ServletContextHandler context =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Jersey 1 servlet — acts as the JAX-RS front controller
        ServletHolder jersey = new ServletHolder(new ServletContainer());
        jersey.setInitParameter(
                "com.sun.jersey.config.property.packages",
                "com.smartcampus");
        jersey.setInitParameter(
                "com.sun.jersey.api.json.POJOMappingFeature",
                "true");
        // Map Jersey to /api/v1/* to match @ApplicationPath semantics
        context.addServlet(jersey, "/api/v1/*");

        server.setHandler(context);
        server.start();

        LOGGER.info("Smart Campus API started at http://localhost:" + PORT + "/api/v1");
        LOGGER.info("Press Ctrl+C to stop.");

        server.join();
    }
}
