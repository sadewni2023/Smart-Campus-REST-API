package com.smartcampus.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry point for the Smart Campus API.
 * Implements Service Discovery as per Richardson Maturity Model Level 3.
 * Provides a dynamic hub of absolute HATEOAS links to help clients navigate available resources.
 */
@Path("/")
public class DiscoveryResource {

    @Context
    private UriInfo uriInfo;

    /**
     * Handles GET requests to the API root.
     * Generates a structural map of the API containing dynamic links to collections.
     * @return A map with system metadata and resource collection URIs.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getDiscovery() {
        Map<String, Object> discovery = new HashMap<>();
        discovery.put("name", "Smart Campus Sensor & Room Management API");
        discovery.put("apiVersion", "1.0.0");
        discovery.put("administrativeContact", "support@smartcampus.example.com");
        
        Map<String, String> collections = new HashMap<>();
        // Dynamically build absolute URLs based on current deployment for HATEOAS compliance
        collections.put("rooms", uriInfo.getBaseUriBuilder().path(RoomResource.class).build().toString());
        collections.put("sensors", uriInfo.getBaseUriBuilder().path(SensorResource.class).build().toString());
        
        discovery.put("collections", collections);
        return discovery;
    }

    /**
     * Hidden debug endpoint to simulate a server-side logic error.
     * Used exclusively for coursework evidence to verify the GlobalExceptionMapper.
     */
    @GET
    @Path("/debug/error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerError() {
        throw new RuntimeException("Simulated Server-Side Logic Error for Coursework Evidence.");
    }
}
