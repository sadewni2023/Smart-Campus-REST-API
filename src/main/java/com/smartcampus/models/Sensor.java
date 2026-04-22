package com.smartcampus.models;

import java.util.HashMap;
import java.util.Map;

public class Sensor {
    private String id; // Unique identifier, e.g., "TEMP-001"
    private String type; // Category, e.g., "Temperature", "Occupancy", "CO2"
    private String status; // Current state: "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private double currentValue; // The most recent measurement recorded
    private String roomId; // Foreign key linking to the Room where the sensor is located
    private Map<String, String> links = new HashMap<>(); // HATEOAS links

    // Default constructor for JSON-B/Jackson
    public Sensor() {}

    public Sensor(String id, String type, String status, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.roomId = roomId;
        this.currentValue = 0.0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Map<String, String> getLinks() { return links; }
    public void setLinks(Map<String, String> links) { this.links = links; }
}
