# Smart Campus — Sensor & Room Management API

**Student ID:** W2121304  
**Name:** Alankarage Sithika Jayawickrama  
**Module:** 5COSC022W — Client-Server Architectures  
**University:** University of Westminster  
**Stack:** JAX-RS (Jersey 1.x) · Jetty 9 (embedded) · Jackson · Java 11  
**Base URL:** `http://localhost:8080/api/v1`

---

## API Design Overview

The Smart Campus API manages physical **Rooms** and the **Sensors** inside them (temperature, CO2, occupancy, lighting). Every sensor maintains a timestamped **reading history**. The API follows REST principles throughout: resource-based URLs, standard HTTP verbs, meaningful status codes, and JSON bodies for both success and error responses.

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

| ID       | Type        | Status      | Room    |
|----------|-------------|-------------|---------|
| TEMP-001 | Temperature | ACTIVE      | LIB-301 |
| CO2-001  | CO2         | ACTIVE      | LIB-301 |
| OCC-001  | Occupancy   | MAINTENANCE | LAB-102 |
| TEMP-002 | Temperature | ACTIVE      | ENG-201 |

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

### 1. Discovery endpoint — GET /api/v1

```bash
curl -s http://localhost:8080/api/v1
```

### 2. Create a new room — POST /api/v1/rooms

```bash
curl -s -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-001","name":"Main Hall","capacity":200}'
```

### 3. Get all rooms — GET /api/v1/rooms

```bash
curl -s http://localhost:8080/api/v1/rooms
```

### 4. Delete a room with sensors — expect 409 Conflict

```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 5. Register a sensor with invalid roomId — expect 422

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"GHOST-999"}'
```

### 6. Filter sensors by type — GET /api/v1/sensors?type=CO2

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 7. Post a reading to an ACTIVE sensor — expect 201

```bash
curl -s -i -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.4}'
```

### 8. Post a reading to a MAINTENANCE sensor — expect 403

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 12.0}'
```

---

## Conceptual Report — Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle and In-Memory Data Management

By default, JAX-RS creates a new instance of each resource class for every incoming HTTP request. This is called request-scoped lifecycle. The practical consequence is that any instance field declared inside RoomResource or SensorResource is initialised fresh on every request — any data stored there would vanish the moment the request is finished.

To solve this, all shared state in this project lives inside the DataStore singleton — a class with a private static final instance that is created once when the JVM starts and lives for the entire lifetime of the server. Every resource class fetches it via DataStore.getInstance().

For thread safety, the store uses ConcurrentHashMap instead of a plain HashMap. The server handles multiple HTTP requests simultaneously on separate threads. A plain HashMap is not thread-safe — two threads writing to it at the same moment can corrupt its internal structure. ConcurrentHashMap handles concurrent reads and writes safely without needing a global lock, keeping the API responsive under load.

---

### Part 1.2 — HATEOAS and Hypermedia-Driven APIs

HATEOAS (Hypermedia as the Engine of Application State) is the principle that API responses should include links to related resources and available actions, similar to how a web page has hyperlinks. A client that calls GET /api/v1 receives a _links map telling it exactly where /rooms and /sensors are, without needing to consult any external documentation.

This benefits client developers in two key ways. First, it lowers the barrier to entry — a developer can explore the entire API just by following links. Second, it makes clients more resilient to change: if the server moves a path, clients that navigate via links adapt automatically, whereas clients with hard-coded paths break immediately.

---

### Part 2.1 — ID-Only vs. Full Objects in List Responses

Returning only IDs such as LIB-301 and LAB-102 produces a small, fast response but forces the client to make a separate GET request per ID to retrieve usable data — expensive over slow or mobile networks. Returning full room objects gives the client everything it needs in a single round-trip and allows it to render a complete table immediately. The trade-off is a larger payload. This project returns full objects because campus management systems typically need name, capacity, and sensor count in room listings, making the extra payload worthwhile.

---

### Part 2.2 — Idempotency of DELETE

Yes, this implementation is idempotent. REST idempotency means that making the same request multiple times leaves the server in the same state. The first DELETE /rooms/HALL-001 removes the room and returns 204 No Content. A second identical request finds no room to remove and returns 404 Not Found. In both cases the server state is identical — the room does not exist. The client's goal of the room not existing is achieved by both calls. REST idempotency is about server state, not response codes.

---

### Part 3.1 — @Consumes and Content-Type Mismatches

@Consumes(MediaType.APPLICATION_JSON) is a contract declaration that this method will only accept requests carrying the header Content-Type: application/json. If a client sends Content-Type: text/plain or Content-Type: application/xml, JAX-RS intercepts the request before the method body is ever reached and automatically returns HTTP 415 Unsupported Media Type. This protects the method from receiving data it cannot parse and communicates a clear contract violation to the client with no extra code required.

---

### Part 3.2 — @QueryParam vs. Path Parameter for Filtering

A path-based design like /sensors/type/CO2 encodes the filter value into the resource identity. This makes it impossible to express no filter at the same URL, and combining filters produces awkward nested paths like /sensors/type/CO2/status/ACTIVE. It also implies that CO2 is a sub-resource of sensors, which is semantically wrong.

@QueryParam at /sensors?type=CO2 treats the collection as a fixed resource and the query string as an optional view over it. This is semantically correct, the filter is optional by nature, and multiple filters compose naturally and readably such as ?type=CO2&status=ACTIVE. Query parameters are the standard REST convention for filtering, searching, sorting, and pagination precisely because they do not alter resource identity.

---

### Part 4.1 — Sub-Resource Locator Pattern

Without the sub-resource locator, all nested paths would be declared as methods inside SensorResource, creating a monolithic controller where unrelated code is mixed together and difficult to test in isolation.

The sub-resource locator pattern solves this by having SensorResource contain only a single method for the /readings path segment, which returns a new instance of SensorReadingResource. JAX-RS then delegates all further URL matching and HTTP method dispatch to that class. Each class has one clearly defined responsibility, SensorReadingResource can be tested independently, and adding a new sub-resource such as /sensors/{id}/alerts requires only a new class and locator method with no changes to existing code.

---

### Part 5.2 — Why 422 is More Semantically Accurate Than 404

404 Not Found means the URL the client requested does not exist on the server. When a client sends POST /api/v1/sensors with a roomId that points to a non-existent room, the URL /api/v1/sensors is perfectly valid — the endpoint exists and received the request. The problem is inside the JSON body: a field value references a dependency that cannot be resolved.

422 Unprocessable Entity was specifically designed for this situation. It tells the client that the request arrived correctly and the JSON is syntactically valid, but the content is semantically wrong — fix something inside the body. This is immediately actionable. Using 404 here would be misleading because a developer seeing 404 would check their URL first, wasting time, before realising the URL is correct.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

Returning raw Java stack traces to external API consumers exposes four categories of sensitive information.

First, internal architecture. Class and package names like com.smartcampus.resource.RoomResource.deleteRoom reveal the internal structure of the application to an attacker.

Second, library versions. Stack traces contain fully qualified library names and versions such as org.eclipse.jetty 9.4.53. An attacker cross-references these against public CVE databases to find known vulnerabilities for those exact versions.

Third, file system paths. Absolute paths in traces reveal the server directory structure, providing a starting point for directory traversal attacks.

Fourth, business logic flow. The ordered sequence of method calls shows exactly how the application processes a specific type of request, allowing an attacker to find and deliberately trigger error conditions to skip validation or expose data.

The GlobalExceptionMapper addresses all of these risks by logging the full exception server-side only and returning a generic 500 message to the client.

---

### Part 5.5 — Filters vs. Manual Logging in Every Resource Method

Inserting Logger.info() calls manually into every resource method has four serious drawbacks compared to using a JAX-RS filter.

It violates DRY because the same log format code is duplicated across every method in every resource class. A format change requires editing dozens of files.

It misses requests because if a request is rejected by an ExceptionMapper before reaching a resource method, any Logger.info() inside the method is never executed. The filter fires for every request regardless of which code path is taken.

It mixes concerns because resource methods should contain business logic only. Embedding logging inside them couples infrastructure concerns to application logic.

It does not scale because adding authentication, CORS headers, or rate limiting later means creating filters anyway. Starting with the filter pattern from the beginning means the architecture is already prepared for these extensions with no refactoring required.
