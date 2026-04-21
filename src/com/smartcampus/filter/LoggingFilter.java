package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Part 5.5 — API Request and Response Logging Filter
 *
 * This single class implements BOTH ContainerRequestFilter (fires when a request
 * arrives) and ContainerResponseFilter (fires just before a response is sent back).
 *
 * The @Provider annotation tells Jersey to register this automatically.
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
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Called when a request arrives — before it reaches the resource method.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info(String.format("[REQUEST]  %-7s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    /**
     * Called just before the response is sent back to the client.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOG.info(String.format("[RESPONSE] %-7s %s  →  %d",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus()));
    }
}
