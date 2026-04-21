package com.smartcampus.application;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Logger;

/**
 * Application entry point — starts an embedded Jetty 9 HTTP server.
 *
 * No external application server (Tomcat, GlassFish) is needed.
 * Just run this class and the API starts immediately on port 8080.
 *
 * In NetBeans: right-click Main.java > Run File  (or press Shift+F6)
 *
 * The API will be available at:  http://localhost:8080/api/v1
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        Server server = new Server(PORT);

        // Set up the servlet context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Jersey 1.x servlet — this is the JAX-RS engine that handles all requests
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer());

        // Tell Jersey which package to scan for @Path resources and @Provider classes
        jerseyServlet.setInitParameter(
                "com.sun.jersey.config.property.packages",
                "com.smartcampus");

        // Enable automatic POJO -> JSON conversion
        jerseyServlet.setInitParameter(
                "com.sun.jersey.api.json.POJOMappingFeature", "true");

        // Map Jersey to handle all requests under /api/v1/
        context.addServlet(jerseyServlet, "/api/v1/*");
        server.setHandler(context);

        server.start();

        LOG.info("=================================================");
        LOG.info("  Smart Campus API is running!");
        LOG.info("  URL: http://localhost:" + PORT + "/api/v1");
        LOG.info("  Press Ctrl+C to stop.");
        LOG.info("=================================================");

        server.join(); // Keep the server alive
    }
}
