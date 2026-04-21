package com.smartcampus.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application bootstrap.
 *
 * @ApplicationPath sets the root URL for every resource in this application.
 * Jersey scans the classpath for @Path-annotated classes automatically when
 * getClasses() / getSingletons() are not overridden, so no manual registration
 * of resource classes is needed here.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Empty body: Jersey auto-discovers all @Path resources and @Provider classes
    // in the same package tree.
}
