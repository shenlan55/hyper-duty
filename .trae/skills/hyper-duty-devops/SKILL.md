---
name: "hyper-duty-devops"
description: "Manages project operations, deployment, and integration testing for the Hyper Duty system. Invoke when working on project setup, build processes, deployment, or integration testing."
---

# Hyper Duty DevOps

This skill specializes in managing project operations, deployment processes, and integration testing for the Hyper Duty system.

## Core Responsibilities

- **Project Setup**: Configure development environments and project dependencies
- **Build Processes**: Manage frontend and backend build processes
- **Deployment**: Handle application deployment to different environments
- **Integration Testing**: Coordinate frontend-backend integration testing
- **Database Management**: Handle database migrations and seed data
- **Monitoring**: Set up application monitoring and logging
- **Performance Testing**: Conduct load testing and performance optimization
- **Security Scanning**: Perform security vulnerability scanning

## Key Files and Directories

### Build Configuration
- `pom.xml` - Maven build configuration for backend
- `frontend/package.json` - npm package configuration for frontend
- `frontend/vite.config.js` - Vite build configuration

### Deployment Configuration
- `Dockerfile` - Docker container configuration
- `docker-compose.yml` - Docker Compose configuration
- `frontend/nginx.conf` - Nginx configuration for frontend

### Database Scripts
- `src/main/resources/sql/hyper_duty_ddl.sql` - Database schema creation
- `src/main/resources/sql/hyper_duty_dml.sql` - Database seed data

### Environment Configuration
- `.env` - Environment variables
- `.env.template` - Environment variables template

### Test Files
- `test/` - Test cases and scenarios
  - `leave_approval_test_cases.md` - Leave approval test scenarios
  - `substitute_approval_test_cases.md` - Substitute approval test scenarios
  - `test_cases.md` - General test cases

## Development Workflow

### Backend Development Workflow
1. **Environment Setup**: Install JDK 17 and Maven 3.8+
2. **Dependency Installation**: Run `mvn clean install` to install dependencies
3. **Database Initialization**: Execute SQL scripts to set up database
4. **Local Development**: Run `mvn spring-boot:run` to start backend server
5. **Testing**: Run `mvn test` to execute unit tests
6. **Build**: Run `mvn clean package` to build backend artifact

### Frontend Development Workflow
1. **Environment Setup**: Install Node.js 16.x or 18.x
2. **Dependency Installation**: Run `npm install` in frontend directory
3. **Local Development**: Run `npm run dev` to start frontend development server
4. **Testing**: Run `npm test` to execute frontend tests
5. **Build**: Run `npm run build` to build frontend artifact

### Integration Testing Workflow
1. **Start Backend**: Run backend server on localhost:8080
2. **Start Frontend**: Run frontend server on localhost:5173
3. **API Testing**: Test REST APIs using Postman or curl
4. **End-to-End Testing**: Test complete user flows across frontend and backend
5. **Performance Testing**: Test system performance under load

### Deployment Workflow
1. **Build Artifacts**: Build both frontend and backend artifacts
2. **Docker Build**: Build Docker images for both services
3. **Docker Compose**: Use docker-compose to orchestrate services
4. **Environment Configuration**: Set up environment variables for target environment
5. **Deployment**: Deploy to target environment
6. **Health Check**: Verify services are running correctly

## Common Issues and Solutions

1. **Dependency Conflicts**: Use compatible versions of dependencies
2. **Port Conflicts**: Ensure backend (8080) and frontend (5173) ports are available
3. **Database Connection Issues**: Verify database credentials and connection settings
4. **CORS Errors**: Check CORS configuration in backend
5. **Build Failures**: Check for compilation errors and missing dependencies

## Performance Optimization

1. **Database Optimization**: Use appropriate indexes and optimize queries
2. **Backend Optimization**: Use caching and thread pooling
3. **Frontend Optimization**: Minify assets and optimize images
4. **Deployment Optimization**: Use load balancing and auto-scaling

## Security Best Practices

1. **Dependency Scanning**: Regularly scan dependencies for vulnerabilities
2. **Security Configuration**: Follow secure configuration practices
3. **Access Control**: Implement proper access control mechanisms
4. **Encryption**: Use HTTPS and encrypt sensitive data
5. **Audit Logging**: Maintain comprehensive audit logs

## Monitoring and Maintenance

1. **Application Logs**: Configure centralized logging
2. **Health Checks**: Implement health check endpoints
3. **Performance Monitoring**: Set up performance monitoring
4. **Backup Strategies**: Implement regular database backups
5. **Disaster Recovery**: Develop disaster recovery plans

## Example Commands

### Backend Commands
```bash
# Start development server
mvn spring-boot:run

# Build backend
mvn clean package

# Run tests
mvn test

# Install dependencies
mvn clean install
```

### Frontend Commands
```bash
# Start development server
npm run dev

# Build frontend
npm run build

# Install dependencies
npm install

# Run tests
npm test
```

### Docker Commands
```bash
# Build Docker image
docker build -t hyper-duty .

# Run Docker container
docker run -p 8080:8080 hyper-duty

# Start all services with Docker Compose
docker-compose up -d

# Stop all services
docker-compose down
```

### Database Commands
```bash
# Initialize database
mysql -u root -p hyper_duty < src/main/resources/sql/hyper_duty_ddl.sql
mysql -u root -p hyper_duty < src/main/resources/sql/hyper_duty_dml.sql

# Backup database
mysqldump -u root -p hyper_duty > backup.sql

# Restore database
mysql -u root -p hyper_duty < backup.sql
```

## Integration Testing Checklist

1. **API Integration**: Verify all frontend API calls work correctly
2. **Authentication Flow**: Test complete login/logout flow
3. **CRUD Operations**: Test create, read, update, delete operations
4. **Error Handling**: Test error scenarios and error messages
5. **Performance**: Test system response times under normal load
6. **Security**: Test authentication and authorization mechanisms
7. **Data Integrity**: Verify data consistency across frontend and backend
8. **Edge Cases**: Test boundary conditions and edge cases

## Deployment Checklist

1. **Environment Configuration**: Verify environment variables
2. **Database Setup**: Ensure database is properly configured
3. **Service Connectivity**: Verify services can communicate
4. **SSL Configuration**: Ensure HTTPS is properly configured
5. **Load Balancing**: Verify load balancing configuration
6. **Monitoring Setup**: Ensure monitoring is in place
7. **Backup Configuration**: Verify backup processes
8. **Disaster Recovery**: Test disaster recovery procedures

## Troubleshooting Guide

### Common Backend Issues
- **Port Already in Use**: Stop conflicting processes or change port
- **Database Connection Failed**: Check database credentials and network connectivity
- **Dependency Resolution Failed**: Clear Maven cache and retry
- **Security Configuration Error**: Check Spring Security configuration

### Common Frontend Issues
- **CORS Error**: Check backend CORS configuration
- **Build Failed**: Check for syntax errors and missing dependencies
- **API Response Error**: Verify API endpoints and response formats
- **UI Rendering Issues**: Check Vue component syntax and Element Plus usage

### Common Deployment Issues
- **Docker Build Failed**: Check Dockerfile and dependencies
- **Container Startup Failed**: Check logs for error messages
- **Network Connectivity**: Verify network configuration and firewalls
- **Resource Constraints**: Check memory and CPU limits

This guide provides a comprehensive overview of DevOps practices for the Hyper Duty system, ensuring smooth development, testing, and deployment processes.