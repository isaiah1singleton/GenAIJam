# Compact Disc REST API

A production-grade Spring Boot REST API for managing a compact disc catalog with a MySQL database backend.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [API Endpoints](#api-endpoints)
- [Deployment](#deployment)
- [Monitoring & Logging](#monitoring--logging)
- [Troubleshooting](#troubleshooting)

## Overview

The Compact Disc REST API is a Spring Boot 2.5.3 application built with Java 11 that provides a complete REST interface for managing a CD catalog. The application allows users to:

- **View all CDs** in the catalog with details (title, artist, track count, price)
- **Retrieve specific CDs** by ID
- **Add new CDs** to the catalog
- **Delete CDs** from the catalog
- **Track related songs** for each CD via a related tracks table

The API is fully documented with Swagger/OpenAPI and supports both local development and containerized Docker deployments.

## Prerequisites

### Local Development
- **Java 11** or higher
- **Maven 3.6+** (for building the application)
- **MySQL 5.7+** or **MySQL 8.0+** (running locally or remotely accessible)

### Docker Deployment
- **Docker** (version 19.03+)
- **Docker Compose** (version 1.25+)
- No local Java or Maven installation required

## Installation & Setup

### 1. Clone and Build

```bash
# Navigate to the project directory
cd challenges/no-readme

# Build the application
mvn clean install
```

### 2. Local Database Setup

Before running the application, initialize the MySQL database:

```bash
# Connect to MySQL
mysql -u root -p

# Run the SQL initialization script
mysql -u root -p < sql/createTables.sql
```

The schema creates two tables:
- **compact_discs**: Stores CD information (id, title, artist, tracks, price)
- **tracks**: Stores individual track information for each CD (id, cd_id, title)

## API Endpoints

### Base URL
```
http://localhost:8080/api/compactdiscs
```

### Endpoints

#### GET All CDs
```
GET /api/compactdiscs
```
Returns all CDs in the catalog.

**Response:**
```json
[
  {
    "id": 9,
    "title": "Is This It",
    "artist": "The Strokes",
    "tracks": 11,
    "price": 13.99
  }
]
```

#### GET CD by ID
```
GET /api/compactdiscs/{id}
```
Returns a specific CD by its ID.

#### GET CD by ID with 404 Handling
```
GET /api/compactdiscs/404/{id}
```
Returns a specific CD or a 404 Not Found status if the CD doesn't exist.

#### POST - Add New CD
```
POST /api/compactdiscs
Content-Type: application/json

{
  "title": "Sweet Caroline",
  "artist": "Neil Diamond",
  "price": 13.99,
  "tracks": 1
}
```

#### DELETE CD by ID
```
DELETE /api/compactdiscs/{id}
```
Deletes a CD from the catalog by ID.

#### DELETE CD by Object
```
DELETE /api/compactdiscs
Content-Type: application/json

{
  "id": 9,
  "title": "Is This It",
  "artist": "The Strokes",
  "tracks": 11,
  "price": 13.99
}
```

### API Documentation
The API includes integrated Swagger UI for interactive documentation:
```
http://localhost:8080/swagger-ui.html
```

## Deployment

### Local Development Deployment

1. **Build the application:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   java -jar target/CompactDiscRestDataBoot-0.0.1-SNAPSHOT.jar
   ```

3. **Verify it's running:**
   ```bash
   curl http://localhost:8080/api/compactdiscs
   ```

### Configuration

#### Local Configuration (application.properties)
- **Database URL:** `jdbc:mysql://localhost:3306/conygre`
- **Database User:** `root`
- **Database Password:** `c0nygre1`
- **Server Port:** `8080` (can be changed via `server.port` property)
- **Logging:** Output to console and `myapplication.log` file

### Docker Deployment

For containerized deployment, use the Docker profile configuration.

#### Docker Compose Setup (Recommended)

Create a `docker-compose.yml`:

```yaml
version: '3.8'

services:
  cddb:
    image: mysql:8.0
    container_name: cd_database
    environment:
      MYSQL_ROOT_PASSWORD: secret123
      MYSQL_DATABASE: conygre
    volumes:
      - ./sql/createTables.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"
    networks:
      - cd-network

  api:
    build: .
    container_name: cd_api
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - cddb
    networks:
      - cd-network

networks:
  cd-network:
    driver: bridge
```

#### Building and Running with Docker

```bash
# Build the Docker image
docker build -t cd-api:latest .

# Run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f api

# Stop services
docker-compose down
```

#### Docker Configuration (application-docker.properties)
When running in Docker, the application uses the `docker` profile:
- **Database URL:** `jdbc:mysql://cddb:3306/conygre` (service name resolution)
- **Database User:** `root`
- **Database Password:** `secret123`
- **Server Port:** `8080`

## Monitoring & Logging

### Current Logging Setup

The application uses **Log4j2** for logging with the following configuration:

#### Log Output
- **Console Output:** All log messages are printed to stdout with the pattern:
  ```
  YYYY-MM-DD HH:mm:ss LEVEL CLASS:LINE - MESSAGE
  ```
- **File Output:** Logs are also written to `myapplication.log`

#### Log Levels
- **Global Level:** INFO (logs INFO, WARN, ERROR only)
- **Application Package:** `com.conygre.spring.boot` set to INFO level

#### Example Log Entry
```
2026-09-11 14:23:45 INFO CompactDiscController:27 - managed to call a Get request for findAll
```

### Monitoring Capabilities

Currently, the application provides basic logging only. The following **production monitoring features are NOT currently implemented**:

- ❌ **Spring Boot Actuator** - No `/actuator/health` or `/actuator/metrics` endpoints
- ❌ **Application Metrics** - No performance metrics (request counts, response times, etc.)
- ❌ **Health Checks** - No built-in health endpoint for load balancers or Kubernetes
- ❌ **Distributed Tracing** - No APM integration (Jaeger, Zipkin, etc.)
- ❌ **Database Connection Pooling Metrics** - No visibility into connection pool health

### Recommended Production Enhancements

To make this application production-ready with proper observability:

#### 1. Add Spring Boot Actuator
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Enable endpoints in `application.properties`:
```properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
```

#### 2. Add Prometheus Metrics
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Access metrics at: `http://localhost:8080/actuator/prometheus`

#### 3. Integration with ELK Stack
- Ship logs from `myapplication.log` to Elasticsearch via Filebeat
- Visualize and analyze logs in Kibana

#### 4. Container Orchestration Health Checks
Add to Kubernetes deployment:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

### Viewing Logs

#### Local Development
```bash
# View real-time logs
tail -f myapplication.log

# Search for errors
grep ERROR myapplication.log

# Search by class
grep CompactDiscController myapplication.log
```

#### Docker Deployment
```bash
# View application logs
docker-compose logs -f api

# Search logs
docker-compose logs api | grep ERROR
```

## Troubleshooting

### Database Connection Issues

**Problem:** `Connection refused` errors or `Cannot connect to database`

**Solutions:**
1. Verify MySQL is running:
   ```bash
   # Local
   mysql -u root -p -e "SELECT 1"
   
   # Docker
   docker-compose logs cddb
   ```

2. Check credentials in `application.properties` (local) or `application-docker.properties` (Docker)

3. Verify the database and tables exist:
   ```bash
   mysql -u root -p conygre -e "SHOW TABLES;"
   ```

### Port Already in Use

**Problem:** `Port 8080 is already in use`

**Solutions:**
1. Change the port in `application.properties`:
   ```properties
   server.port=8081
   ```

2. Or kill the process using port 8080:
   ```bash
   # Windows
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   
   # Mac/Linux
   lsof -i :8080
   kill -9 <PID>
   ```

### Build Failures

**Problem:** `mvn clean install` fails with dependency errors

**Solutions:**
1. Clear Maven cache:
   ```bash
   rm -rf ~/.m2/repository
   ```

2. Rebuild:
   ```bash
   mvn clean install -U
   ```

### Application Won't Start

**Problem:** Application starts but doesn't accept requests

**Solutions:**
1. Check logs for errors:
   ```bash
   tail -f myapplication.log
   ```

2. Verify the application started successfully:
   ```bash
   curl http://localhost:8080/api/compactdiscs
   ```

3. Check Swagger UI is accessible:
   ```
   http://localhost:8080/swagger-ui.html
   ```

## Contributing

For issues or improvements, please ensure:
- All tests pass: `mvn test`
- Code follows project conventions
- Changes are documented in commit messages

## License

[Add appropriate license information]