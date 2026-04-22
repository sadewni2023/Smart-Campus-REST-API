package com.smartcampus.resources;

import com.smartcampus.dao.SmartCampusDAO;
import com.smartcampus.models.Room;
import com.smartcampus.exceptions.RoomNotEmptyException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Resource class responsible for managing Room entities.
 * Provides endpoints for creating, retrieving, and deleting rooms.
 * Strictly adheres to JAX-RS specifications and uses a Singleton DAO for persistence.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    private final SmartCampusDAO dao = SmartCampusDAO.getInstance();
    
    @Context
    private UriInfo uriInfo;

    /**
     * Retrieves a list of all rooms registered in the system.
     * @return A list of Room objects with HATEOAS links.
     */
    @GET
    public List<Room> getAllRooms() {
        List<Room> rooms = dao.getAllRooms();
        rooms.forEach(this::addLinks);
        return rooms;
    }

    /**
     * Creates a new room in the system.
     * @param room The Room object to create.
     * @return 201 Created on success, or appropriate error code.
     */
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid room data provided.")
                    .build();
        }
        if (dao.getRoomById(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Room with ID " + room.getId() + " already exists.")
                    .build();
        }
        dao.addRoom(room);
        
        java.net.URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();
                
        addLinks(room);
        return Response.status(Response.Status.CREATED)
                .location(location)
                .entity(room)
                .build();
    }

    /**
     * Fetches detailed metadata for a specific room.
     * @param roomId The unique ID of the room.
     * @return The Room object with HATEOAS links.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dao.getRoomById(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        addLinks(room);
        return Response.ok(room).build();
    }

    /**
     * Deletes a room from the system.
     * @param roomId The unique ID of the room.
     * @return 204 No Content on success.
     * @throws RoomNotEmptyException if the room still contains sensors.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dao.getRoomById(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Business Logic Constraint: Cannot delete room if it has sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room " + roomId + " because it still has active sensors assigned to it.");
        }
        
        dao.deleteRoom(roomId);
        return Response.noContent().build();
    }

    /**
     * Injects HATEOAS links into the Room object for dynamic navigation.
     */
    private void addLinks(Room room) {
        Map<String, String> links = new HashMap<>();
        
        // Self link
        links.put("self", uriInfo.getBaseUriBuilder()
                .path(RoomResource.class)
                .path(room.getId())
                .build().toString());
        
        // Link to sensors within this room
        links.put("sensors", uriInfo.getBaseUriBuilder()
                .path(SensorResource.class)
                .queryParam("roomId", room.getId())
                .build().toString());
                
        room.setLinks(links);
    }
}
