package com.smartcampus.resources;

import com.smartcampus.dao.SmartCampusDAO;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import com.smartcampus.exceptions.SensorUnavailableException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Nested resource responsible for handling historical data (readings) for a specific sensor.
 * This resource is reached via the /sensors/{id}/readings path.
 * It manages reading history and ensures the parent sensor's current value is updated.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final SmartCampusDAO dao = SmartCampusDAO.getInstance();
    
    @Context
    private UriInfo uriInfo;

    /**
     * Retrieves all historical readings for the context sensor.
     * @param sensorId Injected by JAX-RS from the parent path segment.
     * @return List of {@link SensorReading} objects.
     */
    @GET
    public List<SensorReading> getReadings(@PathParam("sensorId") String sensorId) {
        return dao.getReadingsForSensor(sensorId);
    }

    @POST
    public Response addReading(@PathParam("sensorId") String sensorId, SensorReading reading) {
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid reading data provided.")
                    .build();
        }
        Sensor sensor = dao.getSensorById(sensorId);
        
        // State Constraint: Cannot accept readings if status is MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is currently in MAINTENANCE and cannot accept new readings.");
        }
        
        // Ensure ID and timestamp are set if not provided
        if (reading.getId() == null) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        
        dao.addReading(sensorId, reading);
        
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(reading.getId())
                .build();
                
        return Response.status(Response.Status.CREATED)
                .location(location)
                .entity(reading)
                .build();
    }
}
