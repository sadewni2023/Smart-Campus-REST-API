package com.smartcampus.exceptions.mappers;

import com.smartcampus.exceptions.RoomNotEmptyException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Conflict");
        error.put("message", exception.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .build();
    }
}
