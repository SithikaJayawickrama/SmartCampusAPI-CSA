package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 *
 * WHY SINGLETON?
 * JAX-RS creates a brand-new instance of each resource class for every single
 * HTTP request. This means any field you declare inside RoomResource would reset
 * to zero/null on every request — you would lose all your data.
 *
 * By putting all data in this singleton with static maps, it lives for the entire
 * lifetime of the server process, and every resource class shares the same data.
 *
 * WHY ConcurrentHashMap?
 * The server can handle multiple HTTP requests at the same time (multi-threaded).
 * A regular HashMap is not thread-safe — two threads writing at the same time can
 * corrupt data. ConcurrentHashMap handles this safely without needing synchronized
 * blocks on every read operation.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    // All rooms: roomId -> Room object
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // All sensors: sensorId -> Sensor object
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Reading history: sensorId -> list of readings for that sensor
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── ROOM OPERATIONS ──────────────────────────────────────────────────────

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void saveRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    // ── SENSOR OPERATIONS ────────────────────────────────────────────────────

    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Make sure a readings list exists for this sensor
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    public boolean deleteSensor(String id) {
        readings.remove(id);
        return sensors.remove(id) != null;
    }

    // ── READING OPERATIONS ───────────────────────────────────────────────────

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    /**
     * Saves a new reading for a sensor and updates the sensor's currentValue.
     * The synchronized block protects the list from concurrent modification
     * (two readings arriving at exactly the same millisecond).
     */
    public void addReading(String sensorId, SensorReading reading) {
        List<SensorReading> list = readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
        synchronized (list) {
            list.add(reading);
        }
        // Side-effect: keep the parent sensor's currentValue up to date
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }

    // ── SEED DATA (demo data so the API works immediately on first run) ───────

    private void seedData() {
        // Rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room r2 = new Room("LAB-102", "Computer Lab Alpha", 30);
        Room r3 = new Room("ENG-201", "Engineering Seminar Room", 20);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",     21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",    412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "LAB-102");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "ACTIVE",     19.8, "ENG-201");

        saveSensor(s1);
        saveSensor(s2);
        saveSensor(s3);
        saveSensor(s4);

        // Link sensors to their rooms
        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("CO2-001");
        r2.getSensorIds().add("OCC-001");
        r3.getSensorIds().add("TEMP-002");
    }
}
