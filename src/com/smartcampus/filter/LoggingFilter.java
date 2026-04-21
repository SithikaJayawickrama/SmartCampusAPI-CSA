package com.smartcampus.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.logging.Logger;

/**
 * Part 5.5 — API Request and Response Logging Filter
 *
 * This single class implements BOTH ContainerRequestFilter (fires when a request
 * arrives) and ContainerResponseFilter (fires just before a response is sent back).
 *
 * Uses Jersey 1.x native filter APIs (com.sun.jersey.spi.container) which are
 * compatible with JAX-RS 1.1 (jsr311-api). The JAX-RS 2.0 container filter
 * interfaces (javax.ws.rs.container.*) are not available in Jersey 1.x.
 *
 * WHY USE A FILTER instead of adding Logger.info() to every resource method?
 *
 *   1. ONE PLACE TO CHANGE
 *      If you want to change the log format, you edit one file. Without a filter,
 *      you'd need to update every single resource method across the whole project.
 *
 *   2. NEVER MISSES A REQUEST
 *      Filters fire for EVERY request — even ones that hit an ExceptionMapper
 *      before reaching a resource method. Logger.info() inside a method would
 *      be skipped entirely if an exception happens early.
 *
 *   3. SEPARATION OF CONCERNS
 *      Resource methods should contain business logic only. Logging is a
 *      "cross-cutting concern" — something that applies everywhere but has
 *      nothing to do with business rules. Filters are the JAX-RS way of
 *      handling cross-cutting concerns cleanly.
 *
 *   4. EASIER TO EXTEND
 *      Need to add authentication? CORS headers? Rate limiting?
 *      All of these are filters too — just add another @Provider class.
 *
 * Console output example:
 *   [REQUEST]  POST   http://localhost:8080/api/v1/rooms
 *   [RESPONSE] POST   http://localhost:8080/api/v1/rooms  →  201
 */
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Called when a request arrives — before it reaches the resource method.
     * Must return the (possibly modified) request to continue the filter chain.
     */
    @Override
    public ContainerRequest filter(ContainerRequest request) {
        LOG.info(String.format("[REQUEST]  %-7s %s",
                request.getMethod(),
                request.getRequestUri()));
        return request;
    }

    /**
     * Called just before the response is sent back to the client.
     * Must return the (possibly modified) response.
     */
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        LOG.info(String.format("[RESPONSE] %-7s %s  \u2192  %d",
                request.getMethod(),
                request.getRequestUri(),
                response.getStatus()));
        return response;
    }
}
