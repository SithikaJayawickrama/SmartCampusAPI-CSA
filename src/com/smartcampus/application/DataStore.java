package com.smartcampus.application;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms           = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors       = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() { seedData(); }

    public static DataStore getInstance() { return INSTANCE; }

    public Collection<Room> getAllRooms()          { return rooms.values(); }
    public Room getRoom(String id)                 { return rooms.get(id); }
    public boolean roomExists(String id)           { return rooms.containsKey(id); }
    public void saveRoom(Room room)                { rooms.put(room.getId(), room); }
    public boolean deleteRoom(String id)           { return rooms.remove(id) != null; }

    public Collection<Sensor> getAllSensors()      { return sensors.values(); }
    public Sensor getSensor(String id)             { return sensors.get(id); }
    public boolean sensorExists(String id)         { return sensors.containsKey(id); }
    public void saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }
    public boolean deleteSensor(String id) {
        readings.remove(id);
        return sensors.remove(id) != null;
    }

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        List<SensorReading> list = readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
        synchronized (list) { list.add(reading); }
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) { sensor.setCurrentValue(reading.getValue()); }
    }

    private void seedData() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room r2 = new Room("LAB-102", "Computer Lab Alpha", 30);
        Room r3 = new Room("ENG-201", "Engineering Seminar Room", 20);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",     412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE",  0.0, "LAB-102");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "ACTIVE",      19.8, "ENG-201");

        saveSensor(s1); saveSensor(s2); saveSensor(s3); saveSensor(s4);

        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("CO2-001");
        r2.getSensorIds().add("OCC-001");
        r3.getSensorIds().add("TEMP-002");
    }
}
