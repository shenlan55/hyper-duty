---
name: "hyper-duty-backend"
description: "Develops and maintains backend services for the Hyper Duty system using Spring Boot, MyBatis, and Spring Security. Invoke when working on backend features, API development, database operations, or security configurations."
---

# Hyper Duty Backend Developer

This skill specializes in developing and maintaining backend services for the Hyper Duty system, focusing on Spring Boot, MyBatis, and Spring Security integration.

## Core Responsibilities

- **API Development**: Create and maintain RESTful APIs using Spring Web
- **Database Integration**: Implement MyBatis Mappers for database operations
- **Security Configuration**: Set up Spring Security and JWT authentication
- **Service Layer Implementation**: Develop business logic in service classes
- **Entity Management**: Create and maintain JPA/MyBatis entities
- **Exception Handling**: Implement global exception handling
- **Performance Optimization**: Optimize database queries and service methods
- **Scheduled Tasks**: Configure and maintain Spring Scheduling tasks
- **Batch Operations**: Implement batch processing for large datasets

## Key Files and Directories

### Core Configuration
- `src/main/java/com/lasu/hyperduty/HyperDutyApplication.java` - Application entry point
- `src/main/resources/application.yml` - Application configuration
- `src/main/java/com/lasu/hyperduty/config/` - Configuration classes
  - `ThreadPoolConfig.java` - Thread pool configuration
  - `ScheduleConfig.java` - Scheduled task configuration
  - `PasswordEncoderConfig.java` - Password encoder configuration

### Security
- `src/main/java/com/lasu/hyperduty/security/` - Security components
  - `SecurityConfig.java` - Spring Security configuration
  - `JwtAuthenticationFilter.java` - JWT authentication filter

### Controllers
- `src/main/java/com/lasu/hyperduty/controller/` - REST controllers
  - `AuthController.java` - Authentication APIs
  - `SysEmployeeController.java` - Employee management APIs
  - `SysDeptController.java` - Department management APIs
  - `SysRoleController.java` - Role management APIs
  - `SysMenuController.java` - Menu management APIs
  - Duty management controllers

### Services
- `src/main/java/com/lasu/hyperduty/service/` - Service interfaces
- `src/main/java/com/lasu/hyperduty/service/impl/` - Service implementations

### Entities
- `src/main/java/com/lasu/hyperduty/entity/` - Database entities

### Mappers
- `src/main/java/com/lasu/hyperduty/mapper/` - MyBatis mapper interfaces
- `src/main/resources/mapper/` - MyBatis XML mapper files

### Common Utilities
- `src/main/java/com/lasu/hyperduty/common/` - Common utilities
  - `ResponseResult.java` - API response structure
  - `ResponseUtil.java` - Response utility methods
  - `PageResult.java` - Pagination result structure

## Development Guidelines

1. **API Design**: Follow RESTful API design principles
   - Use appropriate HTTP methods (GET, POST, PUT, DELETE)
   - Implement proper error handling
   - Return consistent response format

2. **Database Operations**: Use MyBatis best practices
   - Use parameterized queries to prevent SQL injection
   - Implement proper transaction management
   - Optimize queries with indexes and joins

3. **Security**: Follow Spring Security best practices
   - Use BCrypt for password hashing
   - Implement proper JWT token management
   - Configure secure CORS settings

4. **Error Handling**: Implement global exception handling
   - Create custom exception classes
   - Use @ControllerAdvice for global error handling
   - Return consistent error responses

5. **Performance Optimization**:
   - Use connection pooling for database operations
   - Implement caching for frequently accessed data
   - Optimize service methods with proper indexing

## Common Issues and Solutions

1. **Circular Dependencies**: Use setter injection or @Lazy annotation
2. **JWT Token Issues**: Check token expiration and signature validation
3. **Database Performance**: Optimize queries and use appropriate indexes
4. **Security Configuration**: Ensure proper access control and authentication
5. **Scheduled Task Issues**: Check task scheduling configuration and thread pool settings

## Testing Guidelines

- Write unit tests for service methods
- Implement integration tests for API endpoints
- Test error handling scenarios
- Verify security configurations
- Test database operations with different datasets

## Best Practices

- **Code Organization**: Follow modular design principles
- **Documentation**: Add Javadoc comments for public methods
- **Logging**: Implement comprehensive logging
- **Monitoring**: Set up application monitoring
- **Code Reviews**: Conduct regular code reviews

## Example Workflow

1. **Entity Creation**: Create database entities for new features
2. **Mapper Implementation**: Implement MyBatis mappers for database operations
3. **Service Development**: Create service interfaces and implementations
4. **Controller Implementation**: Develop RESTful API controllers
5. **Security Configuration**: Update security settings if needed
6. **Testing**: Write and run tests for new functionality
7. **Deployment**: Prepare for deployment and monitoring