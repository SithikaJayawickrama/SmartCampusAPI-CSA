# Smart Campus — Sensor & Room Management API

**UoW Number:** W2121304
**IIT Number:** 20241119
**Name:** Alankarage Sithika Jayawickrama
**Module:** 5COSC022W — Client-Server Architectures
**Stack:** JAX-RS (Jersey 1.x) · Jetty 9 (embedded) · Jackson · Java 11
**Base URL:** `http://localhost:8080/api/v1`

---

## API Design Overview

The Smart Campus API manages physical Rooms and the Sensors inside them (temperature, CO2, occupancy, lighting). Every sensor maintains a timestamped reading history. The API follows REST principles throughout: resource-based URLs, standard HTTP verbs, meaningful status codes, and JSON bodies for both success and error responses.

### Resource hierarchy
/api/v1
├── /                            Discovery — API metadata and navigation links
├── /rooms                       Room collection
│   └── /{roomId}                Individual room
└── /sensors                     Sensor collection
└── /{sensorId}              Individual sensor
└── /readings            Reading history (sub-resource)
└── /{readingId}     Individual reading

### HTTP status codes

| Code | Used for |
|------|----------|
| 200  | Successful GET |
| 201  | Successful POST (includes Location header) |
| 204  | Successful DELETE |
| 400  | Missing or invalid request fields |
| 403  | Posting a reading to a MAINTENANCE or OFFLINE sensor |
| 404  | Resource not found |
| 409  | Deleting a room that still has sensors |
| 415  | Wrong Content-Type on POST (auto-handled by JAX-RS) |
| 422  | Sensor registered with a roomId that does not exist |
| 500  | Any unexpected server error (no stack trace exposed) |

### Seed data available immediately on startup

| ID | Type | Status | Room |
|----|------|--------|------|
| TEMP-001 | Temperature | ACTIVE | LIB-301 |
| CO2-001 | CO2 | ACTIVE | LIB-301 |
| OCC-001 | Occupancy | MAINTENANCE | LAB-102 |
| TEMP-002 | Temperature | ACTIVE | ENG-201 |

---

## Project Structure
SmartCampusAPI/
├── src/
│   └── com/smartcampus/
│       ├── application/
│       │   ├── DataStore.java
│       │   ├── Main.java
│       │   └── SmartCampusApplication.java
│       ├── model/
│       │   ├── Room.java
│       │   ├── Sensor.java
│       │   ├── SensorReading.java
│       │   └── ApiError.java
│       ├── resource/
│       │   ├── DiscoveryResource.java
│       │   ├── RoomResource.java
│       │   ├── SensorResource.java
│       │   └── SensorReadingResource.java
│       ├── exception/
│       │   ├── RoomNotEmptyException.java
│       │   ├── RoomNotEmptyExceptionMapper.java
│       │   ├── LinkedResourceNotFoundException.java
│       │   ├── LinkedResourceNotFoundExceptionMapper.java
│       │   ├── SensorUnavailableException.java
│       │   ├── SensorUnavailableExceptionMapper.java
│       │   └── GlobalExceptionMapper.java
│       └── filter/
│           └── LoggingFilter.java
├── lib/
├── nbproject/
├── build.xml
└── manifest.mf

---

## How to Build and Run

### Option A — NetBeans (recommended)

1. Open NetBeans IDE
2. Go to File then Open Project
3. Select the SmartCampusAPI folder
4. Press F6 to run
5. The Output window will show:

INFO: Smart Campus API is running!
INFO: URL: http://localhost:8080/api/v1
INFO: Press Ctrl+C to stop.

### Option B — Command line using Ant

```bash
cd SmartCampusAPI
ant run
```

---

## Sample curl Commands

### 1. Discovery endpoint

```bash
curl -s http://localhost:8080/api/v1
```

### 2. Create a new room

```bash
curl -s -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-001","name":"Main Hall","capacity":200}'
```

### 3. Get all rooms

```bash
curl -s http://localhost:8080/api/v1/rooms
```

### 4. Delete a room with sensors — expect 409 Conflict

```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 5. Register sensor with invalid roomId — expect 422

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"GHOST-999"}'
```

### 6. Filter sensors by type

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 7. Post a reading to an ACTIVE sensor

```bash
curl -s -i -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.4}'
```

### 8. Post a reading to MAINTENANCE sensor — expect 403

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 12.0}'
```

---

## Report — Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle

In JAX-RS, a new object of the resource class is created for every HTTP request that comes in. This means if I stored data as a field inside RoomResource, it would be lost after each request because the object gets recreated.

To fix this I used a singleton class called DataStore. This class is created only once when the server starts and stays alive the whole time the server is running. All the resource classes get the same DataStore using DataStore.getInstance(), so the data is shared between requests.

I also used ConcurrentHashMap instead of a normal HashMap because multiple requests can come in at the same time. A normal HashMap can get corrupted if two threads write to it at the same time. ConcurrentHashMap handles this safely.

---

### Part 1.2 — HATEOAS

HATEOAS means the API response includes links that tell the client where it can go next. For example when a client calls GET /api/v1, the response includes links to /rooms and /sensors. This is similar to how a website has a navigation menu.

This is useful because the client does not need to hard-code the URLs. If the server changes a URL, the client can still find it through the links. It also makes it easier for new developers to understand the API without reading long documentation.

---

### Part 2.1 — ID-Only vs. Full Objects

If the API only returns IDs in the list, the client has to make another request for each ID to get the full details. This uses more network requests and is slower.

If the API returns the full objects in the list, the client gets everything it needs in one request. The downside is the response is bigger. For this project I returned full objects because it is more useful for the client to see all the room details at once.

---

### Part 2.2 — Idempotency of DELETE

Yes, DELETE is idempotent in my implementation. Idempotent means calling the same request multiple times gives the same result on the server.

The first DELETE request removes the room and returns 204. If I call it again the room is already gone so it returns 404. In both cases the room does not exist on the server, which is what the client wanted. The status code might be different but the server state is the same.

---

### Part 3.1 — @Consumes and Content-Type

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that this method only accepts JSON. If a client sends a different content type like text/plain or application/xml, JAX-RS automatically rejects the request before it even reaches my method. It returns a 415 Unsupported Media Type response. This protects the method from receiving data it cannot read.

---

### Part 3.2 — @QueryParam vs Path Parameter

Using a path like /sensors/type/CO2 puts the filter inside the URL structure. This is not flexible because you cannot get all sensors without the filter, and adding more filters makes the URL complicated.

Using @QueryParam like /sensors?type=CO2 keeps the collection URL the same and makes the filter optional. You can also combine filters easily like ?type=CO2&status=ACTIVE. This is the standard way to do filtering in REST APIs.

---

### Part 4.1 — Sub-Resource Locator Pattern

Instead of putting all the code for readings inside SensorResource, I created a separate class called SensorReadingResource. The SensorResource class has a locator method that returns an instance of SensorReadingResource when the URL contains /readings.

This is better because each class only handles one thing. SensorReadingResource can be understood and tested on its own. If I want to add more sub-resources in the future I just need to create a new class without changing existing code.

---

### Part 5.2 — Why 422 and not 404

404 means the URL was not found. But when I POST to /sensors with a wrong roomId, the URL /sensors does exist and works fine. The problem is that the roomId value inside the JSON body does not match any room in the system.

422 Unprocessable Entity is more correct here because it means the server understood the request and the JSON was valid, but the content inside could not be processed. It tells the developer to check the field values in the body rather than the URL.

---

### Part 5.4 — Risks of Exposing Stack Traces

If the API returns a Java stack trace to the client it exposes sensitive information. First it shows the class and package names which reveals how the application is structured. Second it shows library names and version numbers which an attacker can use to look up known security problems for those versions. Third it can show file paths on the server which helps attackers understand the server setup. Fourth it shows the order of method calls which can help attackers find ways to break the application.

My GlobalExceptionMapper catches all unexpected errors and returns a simple 500 message to the client. The full error is only written to the server log where only the administrator can see it.

---

### Part 5.5 — Why Use Filters Instead of Manual Logging

If I put Logger.info() inside every resource method I would have to write the same logging code many times. If I wanted to change the log format I would have to edit every method.

Using a filter means the logging code is written once and runs for every request automatically. It also works even when an exception mapper stops the request before it reaches the resource method, which means no requests are missed. Keeping logging in a filter also means the resource methods only contain business logic and are easier to read.