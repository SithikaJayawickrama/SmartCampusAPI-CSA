package com.smartcampus.exception;

import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Part 5.4 — Global Safety Net (catches ALL unhandled exceptions)
 *
 * This mapper catches any Throwable not already handled by a more specific mapper
 * (e.g. NullPointerException, IndexOutOfBoundsException, IllegalStateException).
 *
 * Without this, Jersey would return a raw HTML error page or Java stack trace to
 * the client — exposing sensitive internal details to potential attackers.
 *
 * CYBERSECURITY RISKS of exposing stack traces:
 *
 *   1. INTERNAL CLASS NAMES & PACKAGE STRUCTURE
 *      A trace shows "com.smartcampus.resource.RoomResource.deleteRoom(RoomResource.java:87)"
 *      This tells an attacker exactly how the code is organised and which classes exist.
 *
 *   2. LIBRARY NAMES AND VERSIONS
 *      "org.eclipse.jetty 9.4.53" or "jersey 1.19.4" appear in traces.
 *      Attackers cross-reference these against public CVE vulnerability databases
 *      to find known exploits for that exact version.
 *
 *   3. FILE SYSTEM PATHS
 *      Absolute paths like "/home/student/SmartCampusAPI/src/..." reveal the
 *      server's directory structure, which assists directory traversal attacks.
 *
 *   4. BUSINESS LOGIC FLOW
 *      The sequence of method calls shows how the application processes requests.
 *      An attacker can use this to find and deliberately trigger error conditions
 *      that expose more data or skip validation steps.
 *
 * SOLUTION: Log full details SERVER-SIDE (only admins can see logs) and return
 * only a generic, safe message to the client.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Full stack trace goes to the server log — visible only to administrators
        LOG.log(Level.SEVERE, "Unhandled exception caught by global mapper: " + ex.getMessage(), ex);

        // Client receives only a safe, generic message — no technical details
        ApiError error = new ApiError(
                500,
                "Internal Server Error",
                "An unexpected error occurred on the server. "
                + "Please contact the system administrator if this problem persists."
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
