package com.smartcampus.resources;

import com.smartcampus.dao.SmartCampusDAO;
import com.smartcampus.models.Sensor;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.container.ResourceContext;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resource class responsible for managing Sensors.
 * Supports registration, filtering by shared attributes, and deep-nesting readings.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final SmartCampusDAO dao = SmartCampusDAO.getInstance();

    @Context
    private ResourceContext resourceContext;
    
    @Context
    private UriInfo uriInfo;

    /**
     * Retrieves a list of sensors, optionally filtered by type or roomId.
     * @param type Optional sensor type filter.
     * @param roomId Optional room ID filter.
     * @param status Optional sensor status filter (e.g., ACTIVE, MAINTENANCE).
     * @return A list of matching Sensor objects with HATEOAS links.
     */
    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type, 
                                  @QueryParam("roomId") String roomId,
                                  @QueryParam("status") String status) {
        List<Sensor> sensors = dao.getAllSensors();
        
        java.util.stream.Stream<Sensor> stream = sensors.stream();
        if (type != null && !type.isEmpty()) {
            stream = stream.filter(s -> s.getType().equalsIgnoreCase(type));
        }
        if (roomId != null && !roomId.isEmpty()) {
            stream = stream.filter(s -> s.getRoomId().equalsIgnoreCase(roomId));
        }
        if (status != null && !status.isEmpty()) {
            stream = stream.filter(s -> s.getStatus().equalsIgnoreCase(status));
        }
        
        List<Sensor> filtered = stream.collect(Collectors.toList());
        filtered.forEach(this::addLinks);
        return filtered;
    }

    /**
     * Registers a new sensor and links it to a room.
     * @param sensor The Sensor object to create.
     * @return 201 Created on success, or 422 if the linked roomId does not exist.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getRoomId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid sensor data.").build();
        }

        // Business Logic Validation: Ensure the referenced room exists
        if (dao.getRoomById(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("Referenced roomId " + sensor.getRoomId() + " does not exist.");
        }

        if (dao.getSensorById(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT).entity("Sensor ID already exists.").build();
        }

        dao.addSensor(sensor);
        
        java.net.URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        addLinks(sensor);
        return Response.status(Response.Status.CREATED).location(location).entity(sensor).build();
    }

    /**
     * Fetches detailed state for a specific sensor.
     * @param sensorId The unique ID of the sensor.
     * @return The Sensor object with HATEOAS links.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dao.getSensorById(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        addLinks(sensor);
        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for delegating reading operations.
     * Maps to /sensors/{sensorId}/readings
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dao.getSensorById(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID " + sensorId + " not found.");
        }
        
        // Pass the sensorId to the sub-resource
        return resourceContext.getResource(SensorReadingResource.class);
    }

    /**
     * Injects HATEOAS links for room membership and reading history.
     */
    private void addLinks(Sensor sensor) {
        Map<String, String> links = new HashMap<>();
        
        links.put("self", uriInfo.getBaseUriBuilder()
                .path(SensorResource.class)
                .path(sensor.getId())
                .build().toString());
        
        links.put("room", uriInfo.getBaseUriBuilder()
                .path(RoomResource.class)
                .path(sensor.getRoomId())
                .build().toString());
        
        links.put("readings", uriInfo.getBaseUriBuilder()
                .path(SensorResource.class)
                .path(sensor.getId())
                .path("readings")
                .build().toString());
                
        sensor.setLinks(links);
    }
}
