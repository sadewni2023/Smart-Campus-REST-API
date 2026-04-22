package com.smartcampus.exceptions.mappers;

import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unprocessable Entity");
        error.put("message", exception.getMessage());
        return Response.status(422) // Unprocessable Entity
                .entity(error)
                .build();
    }
}
