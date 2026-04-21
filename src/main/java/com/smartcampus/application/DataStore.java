package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 *
 * JAX-RS creates a new resource instance per request (request-scoped lifecycle by default).
 * To prevent data loss across requests, all shared state lives here as static, thread-safe
 * ConcurrentHashMaps. This avoids race conditions without requiring explicit synchronisation
 * in every resource method.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    // ConcurrentHashMap: thread-safe without locking the entire map on every read
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    // sensorId -> ordered list of readings (synchronised individually on write)
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seed();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── Rooms ────────────────────────────────────────────────────────────────

    public Map<String, Room> getRooms() { return rooms; }

    public Room getRoom(String id) { return rooms.get(id); }

    public void putRoom(Room room) { rooms.put(room.getId(), room); }

    public boolean deleteRoom(String id) { return rooms.remove(id) != null; }

    // ── Sensors ──────────────────────────────────────────────────────────────

    public Map<String, Sensor> getSensors() { return sensors; }

    public Sensor getSensor(String id) { return sensors.get(id); }

    public void putSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public boolean deleteSensor(String id) {
        readings.remove(id);
        return sensors.remove(id) != null;
    }

    // ── Readings ─────────────────────────────────────────────────────────────

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
        // Synchronise on the list to prevent concurrent modification
        synchronized (readings.get(sensorId)) {
            readings.get(sensorId).add(reading);
        }
        // Update parent sensor's currentValue for data consistency
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }

    // ── Seed data ─────────────────────────────────────────────────────────────

    private void seed() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room r2 = new Room("LAB-102", "Computer Lab Alpha", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LAB-102");

        putSensor(s1);
        putSensor(s2);
        putSensor(s3);

        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());
    }
}
