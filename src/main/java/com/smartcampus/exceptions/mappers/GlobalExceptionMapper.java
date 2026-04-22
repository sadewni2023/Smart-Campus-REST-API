package com.smartcampus.exceptions.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // If the exception is a JAX-RS WebApplicationException (like 404, 400, etc.),
        // we should respect its predefined response.
        if (exception instanceof jakarta.ws.rs.WebApplicationException) {
            return ((jakarta.ws.rs.WebApplicationException) exception).getResponse();
        }

        // Detailed logging for truly unexpected logic errors (Internal Server Errors)
        LOGGER.log(java.util.logging.Level.SEVERE, "Unexpected Logic Error Caught: ", exception);

        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected logical error occurred. Please contact the administrator.");
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
    }
}
