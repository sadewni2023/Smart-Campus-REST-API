package com.smartcampus.dao;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe In-memory DAO for Smart Campus data.
 * Used to satisfy the requirement of not using an external database.
 */
public class SmartCampusDAO {
    private static SmartCampusDAO instance;
    
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private SmartCampusDAO() {
        // Initialize with some dummy data if needed
        initializeData();
    }

    public static synchronized SmartCampusDAO getInstance() {
        if (instance == null) {
            instance = new SmartCampusDAO();
        }
        return instance;
    }

    private void initializeData() {
        // Example Room
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        rooms.put(r1.getId(), r1);
        
        // Example Sensor
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", "LIB-301");
        sensors.put(s1.getId(), s1);
        r1.addSensorId(s1.getId());
        
        readings.put(s1.getId(), new ArrayList<>());
    }

    // Room Operations
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        Room room = rooms.get(id);
        if (room != null && (room.getSensorIds() == null || room.getSensorIds().isEmpty())) {
            rooms.remove(id);
            return true;
        }
        return false;
    }

    // Sensor Operations
    public List<Sensor> getAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    public List<Sensor> getSensorsByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
        
        // Update Room linkage
        Room room = rooms.get(sensor.getRoomId());
        if (room != null) {
            room.addSensorId(sensor.getId());
        }
    }

    // Reading Operations
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        List<SensorReading> sensorReadings = readings.getOrDefault(sensorId, new ArrayList<>());
        // Sort by timestamp descending (newest first) for better UX
        return sensorReadings.stream()
                .sorted((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public void addReading(String sensorId, SensorReading reading) {
        List<SensorReading> sensorReadings = readings.get(sensorId);
        if (sensorReadings != null) {
            sensorReadings.add(reading);
            
            // Side effect: update sensor's current value
            Sensor sensor = sensors.get(sensorId);
            if (sensor != null) {
                sensor.setCurrentValue(reading.getValue());
            }
        }
    }
}
