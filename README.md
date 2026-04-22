# ”Smart Campus” Sensor & Room Management API (5COSC022W)

## Overview
This project is a RESTful web service built using **Jakarta RESTful Web Services (JAX-RS)** and **Jersey**. It manages campus infrastructure data including rooms, sensors, and historical readings. 

## Key Features
- **HATEOAS-driven Discovery**: Entry point at `/api/v1` for easy navigation.
- **In-memory Persistence**: Thread-safe data storage using the DAO pattern.
- **Resource Nesting**: Advanced sub-resource management for sensor readings.
- **Robust Error Handling**: Customized HTTP status codes (409, 422, 403, 500) with descriptive JSON bodies.
- **Observability**: Request and response logging via JAX-RS Filters.

## Technology Stack
- **Language**: Java 17+
- **Architecture**: JAX-RS / Jakarta EE 10
- **Implementation**: Jersey 3.1
- **Server**: Embedded Grizzly HTTP Server
- **Build Tool**: Maven

## Build and Launch Instructions

### Prerequisites
- Java 17+ 
- Apache Maven

### Execution
1. Open a terminal in the project root.
2. Build the project:
   ```bash
   mvn clean compile
   ```
3. Run the embedded server:
   ```bash
   mvn exec:java
   ```
   **IMPORTANT:** Keep this terminal window open. The server will stop if you press 'Enter' or close the terminal.
4. The API will be available at `http://localhost:8080/api/v1`.

## Sample CURL Commands

1. **Discovery Endpoint**:
   ```bash
   curl -X GET http://localhost:8080/api/v1
   ```

2. **List All Rooms**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/rooms
   ```

3. **Register a New Sensor**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/sensors \
   -H "Content-Type: application/json" \
   -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "roomId": "LIB-301"}'
   ```

4. **Add a Reading to a Sensor**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
   -H "Content-Type: application/json" \
   -d '{"value": 450.5}'
   ```

5. **Delete a Room (with Safety Logic)**:
   ```bash
   curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
   ```
   *(Note: This will return 409 Conflict if sensors are still assigned to the room.)*

---
## Conceptual Questions & Answers (Final Report)

### Part 1: Service Architecture & Setup

**Question 1: Lifecycle Analysis of JAX-RS Resource Classes**
The default lifecycle of a JAX-RS resource class is **Request-Scoped** (per-request). For every incoming HTTP request, the JAX-RS runtime (e.g., Jersey) instantiates a fresh instance of the resource class and discards it once the response is dispatched.
*   **Architectural Implications**: This design simplifies development by ensuring that request processing is isolated and stateless. However, it means that any instance-level member variables are ephemeral and cannot be used for data persistence.
*   **State Management & Synchronization**: To maintain a consistent application state, we implement a **Singleton Data Access Object (DAO)**. Since multiple request-scoped resource instances may access this shared DAO concurrently, we utilize high-performance, thread-safe data structures such as **ConcurrentHashMap**. This approach avoids the performance bottlenecks associated with global synchronization blocks while guaranteeing that concurrent mutations do not lead to race conditions or data corruption.

**Question 2: Provision of Hypermedia (HATEOAS)**
Hypermedia as the Engine of Application State (HATEOAS) represents the highest tier of REST maturity (**Richardson Maturity Model Level 3**). It promotes an API from a simple data provider to a self-documenting system where clients discover actions via links.
*   **Hallmark of Advanced Design**: HATEOAS significantly reduces the coupling between the client application and the server's specific URI structure. By following links provided in the response body, clients can navigate the API dynamically. This enables **Evolutionary Architecture**, where the backend can reorganize its URI hierarchy without breaking existing clients, provided the semantic link relations (e.g., "rooms", "sensors") remain stable.

---

### Part 2: Room Management

**Question 3: Data Granularity: IDs vs. Full Room Objects**
*   **Returning Only IDs (Minimalist)**: This approach minimizes network bandwidth and reduces serialization overhead. However, it often triggers the **"N+1 Select Problem,"** forcing the client to execute multiple subsequent round-trips to fetch full details for each ID. This can significantly increase total latency and degrade the user experience.
*   **Returning Full Objects (Comprehensive)**: This provides a richer initial response, allowing the client to render information immediately without further round-trips. While the initial payload is larger, it reduces the total number of requests. In a modern "Smart Campus" context, where low-latency interactions are prioritized over small bandwidth savings, returning full objects for collections is generally the superior architectural choice.

**Question 4: Idempotency of the DELETE Operation**
In this implementation, the **DELETE operation is strictly idempotent**. Under REST principles, an operation is idempotent if its side effect on the server state is the same regardless of how many times it is executed.
*   **Justification**: The first DELETE request transition the resource state to "Removed" and returns `204 No Content`. Every subsequent request for the same resource ID will find that it no longer exists and return `404 Not Found`. Despite the variance in HTTP response codes, the **resultant server state** remains identical: the resource is absent. Therefore, the operation remains idempotent as the side effect is non-cumulative.

---

### Part 3: Sensor Operations & Filtering

**Question 5: Negotiation and @Consumes Mismatches**
The `@Consumes(MediaType.APPLICATION_JSON)` annotation acts as a strict guard-rail for the resource method.
*   **Technical Consequences**: If a client attempts to submit data in an unsupported format (e.g., `text/plain` or `application/xml`), the JAX-RS runtime intercepts the request before it reaches the business logic.
*   **Handler Logic**: The server automatically responds with an **HTTP 415 Unsupported Media Type** status. This prevents the system from attempting to invoke a `MessageBodyReader` on incompatible data streams, thereby protecting the application from malformed data and ensuring that only syntactically valid JSON enters the processing pipeline.

**Question 6: URI Design: QueryParam vs. PathParam for Filtering**
Using `@QueryParam` (e.g., `?type=CO2`) is superior to using PathParams for filtering because:
*   **Resource Identity vs. Volatile Attributes**: Path parameters should be reserved for **Resource Identity** (representing the unique taxonomy or location of a resource). Query parameters are designed for **Volatile States** or attributes used to filter a collection.
*   **Avoiding URI Explosion**: Using path segments for filtering (e.g., `/sensors/type/CO2`) leads to a "Static URI Explosion" and makes it extremely difficult to combine filters (e.g., type AND status). Query parameters are optional and combinable by design, making them the standard architectural choice for search and filter operations.

---

### Part 4: Deep Nesting with Sub-Resources

**Question 7: Architectural Benefits of Sub-Resource Locators**
The Sub-Resource Locator pattern allows a parent resource to delegate request handling to a specialized sub-resource class (e.g., delegating reading management to `SensorReadingResource`).
*   **Complexity Management**: This approach adheres to the **Separation of Concerns (SoC)** and **Single Responsibility Principle**. It prevents the development of "Fat Controllers"—massive classes that handle dozens of endpoints. By modularizing the logic, the codebase becomes more manageable, readable, and facilitates easier unit testing and dependency injection for specific resource contexts.

---

### Part 5: Advanced Error Handling & Logging

**Question 8: 422 Unprocessable Entity vs. 404 Not Found**
HTTP **422 Unprocessable Entity** is semantically more accurate than 404 when handling missing linked references within a valid JSON payload.
*   **Justification**: A 404 response implies that the **Endpoint URI** itself (the location of the resource collection) does not exist. However, the URI `/api/v1/sensors` is valid. The error is **semantic**, not structural; the JSON is well-formed, but the instructions it contains (referencing a non-existent Room ID) cannot be followed. 422 explicitly conveys that the server understands the request but cannot process the contained instructions due to logic violations.

**Question 9: Cybersecurity Risks of Internal Stack Traces**
Exposing raw stack traces to external consumers is a critical **Information Disclosure** vulnerability.
*   **Risks & Exploitation**: Stack traces reveal implementation details including the internal directory structure, library versions (**Library Fingerprinting**), and potential weak points in the software supply chain. An attacker can leverage this information to identify known vulnerabilities in those specific library versions or to map out the server's internal architecture to craft highly targeted exploits.

**Question 10: Centralized Observability via JAX-RS Filters**
Utilizing JAX-RS filters (`ContainerRequestFilter` and `ContainerResponseFilter`) for cross-cutting concerns like logging is architecturally superior to manual logging.
*   **Advantages**: It enforces the **DRY (Don't Repeat Yourself)** principle. Centralized logging ensures **Consistency**; every request and response is captured automatically without relying on individual developers to manually insert logging statements. This eliminates the risk of human error where critical endpoints might be missed, and it maintains a clean separation between business logic and observability infrastructure.
