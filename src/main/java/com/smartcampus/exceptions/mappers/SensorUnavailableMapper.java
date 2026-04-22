package com.smartcampus.exceptions.mappers;

import com.smartcampus.exceptions.SensorUnavailableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .build();
    }
}
