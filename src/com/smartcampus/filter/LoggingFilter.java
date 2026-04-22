package com.smartcampus.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.logging.Logger;

public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        LOG.info(String.format("[REQUEST]  %-7s %s",
                request.getMethod(), request.getRequestUri()));
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        LOG.info(String.format("[RESPONSE] %-7s %s  ->  %d",
                request.getMethod(), request.getRequestUri(), response.getStatus()));
        return response;
    }
}
