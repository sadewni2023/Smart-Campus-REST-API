package com.smartcampus.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private String id; // Unique identifier
    private String name; // Human-readable name
    private int capacity; // Maximum occupancy
    private List<String> sensorIds = new ArrayList<>(); // Collection of IDs of sensors deployed in this room
    private Map<String, String> links = new HashMap<>(); // HATEOAS links

    // Default constructor for JSON-B/Jackson
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
    
    public void addSensorId(String sensorId) {
        if (!this.sensorIds.contains(sensorId)) {
            this.sensorIds.add(sensorId);
        }
    }
    
    public void removeSensorId(String sensorId) {
        this.sensorIds.remove(sensorId);
    }

    public Map<String, String> getLinks() { return links; }
    public void setLinks(Map<String, String> links) { this.links = links; }
}
