# TY Multiverse Backend - Agent Guide

## Project Overview

TY Multiverse Backend is a comprehensive Spring Boot application that serves as the core backend service for the TY Multiverse system. It provides REST API endpoints, gRPC services, and manages data persistence for people, weapons, galleries, and other domain entities.

### Architecture
- **Framework**: Spring Boot 3.2.7 with Java 21
- **Database**: PostgreSQL
- **Cache**: Redis
- **Message Queue**: RabbitMQ
- **Protocol**: REST API + gRPC
- **Security**: Keycloak integration

### Key Components
- **People Management**: CRUD operations for character data
- **Weapons System**: Weapon management and damage calculations
- **Gallery Management**: Image and content management
- **Authentication**: Keycloak OAuth2 integration
- **Async Processing**: Background job processing
- **Monitoring**: Health checks and metrics

## Build and Test Commands

### Prerequisites
```bash
# Ensure common module is built first
cd ../ty-multiverse-common
./mvnw clean install

# Verify dependencies
mvn dependency:tree | grep ty-multiverse-common
```

### Build Commands
```bash
# Clean build
./mvnw clean compile

# Generate protobuf sources and compile
./mvnw clean generate-sources compile

# Full build with tests
./mvnw clean generate-sources compile test

# Package (creates JAR)
./mvnw package -DskipTests

# Install to local repository
./mvnw install
```

### Development Mode
```bash
# Start in development mode (auto-restart on changes)
./mvnw spring-boot:run

# Start with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Test Commands
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=YourTestClass

# Run with coverage report
./mvnw test jacoco:report

# Integration tests only
./mvnw test -Pintegration-test
```

## Code Style Guidelines

### Java Code Style
- **Language Level**: Java 21
- **Formatting**: Follow standard Java conventions with 4-space indentation
- **Naming**: camelCase for methods/variables, PascalCase for classes
- **Line Length**: Max 120 characters
- **Imports**: Group by standard java, third party, then project packages

### Specific Conventions
```java
// ✅ Good
@RestController
@RequestMapping("/api/people")
@RequiredArgsConstructor
public class PeopleController {

    private final PeopleService peopleService;

    @GetMapping("/{id}")
    public ResponseEntity<PeopleDto> getPerson(@PathVariable Long id) {
        return ResponseEntity.ok(peopleService.findById(id));
    }
}

// ❌ Avoid
@RestController@RequestMapping("/api/people")public class PeopleController{...}
```

### Package Structure
```
src/main/java/tw/com/tymbackend/
├── core/           # Core business logic
├── module/         # Feature modules
├── config/         # Configuration classes
└── grpc/           # gRPC services
```

## Testing Instructions

### Unit Tests
- Focus on individual components and services
- Use JUnit 5 with Mockito for mocking
- Test coverage should be > 80%
- Name tests descriptively: `methodName_Should_ExpectedBehavior`

### Integration Tests
- Test complete request/response cycles
- Use `@SpringBootTest` with test profiles
- Include database integration tests
- Test external service integrations

### Test Data Management
```java
@Test
@Sql(scripts = "/test-data/cleanup.sql")
void testWithCleanData() {
    // Test implementation
}
```

## Security Considerations

### Authentication & Authorization
- **Keycloak Integration**: All endpoints require valid JWT tokens
- **Role-based Access**: Different roles for admin/user operations
- **Token Validation**: Verify token expiration and claims

### Data Protection
- **Input Validation**: Validate all user inputs using Bean Validation
- **SQL Injection Prevention**: Use parameterized queries and JPA
- **XSS Protection**: Sanitize user-generated content
- **CSRF Protection**: Implemented for state-changing operations

### Secure Headers
- **CORS Configuration**: Properly configured for frontend domain
- **Security Headers**: HSTS, CSP, X-Frame-Options enabled
- **HTTPS Only**: Enforce HTTPS in production

## Additional Instructions

### Commit Message Guidelines
```bash
# Format: <type>(<scope>): <description>

feat(people): add new character creation endpoint
fix(auth): resolve token validation issue
docs(readme): update API documentation
test(people): add integration tests for CRUD operations
refactor(service): improve error handling in PeopleService
```

### Pull Request Process
1. **Branch Naming**: `feature/`, `fix/`, `refactor/` prefixes
2. **Code Review**: All PRs require at least one approval
3. **Testing**: Ensure all tests pass before merge
4. **Documentation**: Update relevant docs for API changes

### Deployment Steps

#### Local Deployment
```bash
# 1. Start dependencies
docker-compose -f docker-compose.dev.yml up -d

# 2. Build and start application
./mvnw clean package -DskipTests
java -jar target/ty-multiverse-backend.jar
```

#### Production Deployment
```bash
# 1. Build optimized JAR
./mvnw clean package -Pprod -DskipTests

# 2. Deploy with proper environment variables
java -jar target/ty-multiverse-backend.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

### Performance Considerations
- **Database Indexing**: Ensure proper indexes on frequently queried columns
- **Connection Pooling**: HikariCP configured for optimal performance
- **Caching Strategy**: Redis for session storage and frequently accessed data
- **Async Processing**: Use `@Async` for long-running operations

### Troubleshooting
- **Common Issues**: Check logs for stack traces and error messages
- **Database Connections**: Verify PostgreSQL connectivity and credentials
- **gRPC Services**: Ensure protobuf files are properly generated
- **Memory Issues**: Monitor JVM heap usage in production

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tymultiverse
DB_USER=your_user
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Keycloak
KEYCLOAK_URL=https://your-keycloak.com
KEYCLOAK_REALM=your-realm
KEYCLOAK_CLIENT_ID=your-client-id
KEYCLOAK_CLIENT_SECRET=your-secret
```
