package com.smartcampus.application;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.smartcampus.exception.GlobalExceptionMapper;
import com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.exception.RoomNotEmptyExceptionMapper;
import com.smartcampus.exception.SensorUnavailableExceptionMapper;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        DefaultResourceConfig config = new DefaultResourceConfig();

        // Jackson JSON provider — handles Map<String,Object> and POJO serialisation
        config.getSingletons().add(new JacksonJsonProvider());

        // Resource classes
        config.getClasses().add(DiscoveryResource.class);
        config.getClasses().add(RoomResource.class);
        config.getClasses().add(SensorResource.class);

        // Exception mappers
        config.getClasses().add(RoomNotEmptyExceptionMapper.class);
        config.getClasses().add(LinkedResourceNotFoundExceptionMapper.class);
        config.getClasses().add(SensorUnavailableExceptionMapper.class);
        config.getClasses().add(GlobalExceptionMapper.class);

        // Logging filter
        config.getClasses().add(LoggingFilter.class);

        // Enable POJO JSON mapping
        config.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);

        Server server = new Server(PORT);

        ServletContextHandler context =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Map to root so /api/v1 and /api/v1/rooms etc all work
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
        context.addServlet(jerseyServlet, "/*");
        server.setHandler(context);

        server.start();

        LOG.info("=================================================");
        LOG.info("  Smart Campus API is running!");
        LOG.info("  URL: http://localhost:" + PORT + "/api/v1");
        LOG.info("  Press Ctrl+C to stop.");
        LOG.info("=================================================");

        server.join();
    }
}
