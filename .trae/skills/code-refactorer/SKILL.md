---
name: "code-refactorer"
description: "Refactors Hyper Duty system code following best practices. Optimizes backend with exception handling, DTO/VO, API versioning and optimizes frontend with component splitting and constant management."
---

# Code Refactorer

This skill specializes in refactoring Hyper Duty system code following architecture best practices.

## Core Responsibilities

- **Backend Refactoring**: Implement unified exception handling, DTO/VO patterns, API versioning
- **Frontend Refactoring**: Split large components into smaller reusable ones, manage constants
- **Performance Optimization**: Optimize API calls, avoid N+1 problems, use caching
- **Code Quality**: Improve maintainability, readability, and follow project conventions

## Refactoring Checklist

### Backend Refactoring Steps

1. **Unified Exception Handling**
   - Create `BusinessException` class
   - Create `GlobalExceptionHandler` with @RestControllerAdvice
   - Handle validation, business, and system exceptions

2. **DTO/VO Design**
   - Create QueryDTO for query parameters
   - Create VO for view objects (includes permissions)
   - Create CreateDTO and UpdateDTO for input validation
   - Use jakarta.validation annotations

3. **API Versioning**
   - Create new controller in `controller/v1/` folder
   - Path prefix: `/api/v1/`
   - Keep old controllers for backward compatibility

4. **Service Layer Optimization**
   - Add methods to return VO with permissions
   - Implement convertToVO method for entity -> VO conversion
   - Optimize queries to avoid N+1 problems

### Frontend Refactoring Steps

1. **Constant Management**
   - Create `frontend/src/constants/` folder
   - Extract magic numbers to constants
   - Create status/type mappings (e.g., TASK_STATUS_MAP)

2. **Component Splitting**
   - Split large components (over 500 lines)
   - Extract search form: `{Module}SearchForm.vue`
   - Extract dialogs: `{Module}EditDialog.vue`, `{Module}DetailDialog.vue`
   - Keep main component focused on orchestration

3. **State Management**
   - Use Pinia for shared state
   - Keep component props clean
   - Use emits for child-to-parent communication

## Key Files and Components

### Backend Files to Check

```
src/main/java/com/lasu/hyperduty/
├── exception/
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── dto/
│   └── {module}/
│       ├── {Entity}QueryDTO.java
│       ├── {Entity}VO.java
│       ├── {Entity}CreateDTO.java
│       └── {Entity}UpdateDTO.java
├── controller/v1/
│   └── {Module}V1Controller.java
└── service/
    ├── {Module}Service.java
    └── impl/
        └── {Module}ServiceImpl.java
```

### Frontend Files to Check

```
frontend/src/
├── constants/
│   └── {module}.js
├── components/
│   ├── {Module}SearchForm.vue
│   ├── {Module}EditDialog.vue
│   └── {Module}DetailDialog.vue
└── views/
    └── {module}/
        └── {Module}List.vue
```

## Development Guidelines

1. **API Response Handling**: Follow project's response interceptor pattern
2. **Parameter Validation**: Use jakarta.validation on DTOs
3. **Cache Management**: Follow project cache guidelines (see project_rules.md)
4. **Component Design**: Follow Vue 3 Composition API best practices
5. **Type Safety**: Use TypeScript for better type checking (if applicable)

## Common Refactoring Scenarios

### 1. Task List Refactoring (Completed Example)

**Before**: Large monolithic TaskList.vue component + basic backend without DTO/VO
**After**:
- Backend: DTO/VO + unified exception handling + API v1
- Frontend: TaskSearchForm.vue + TaskEditDialog.vue + TaskProgressUpdateDialog.vue + constants

### 2. Project Management Refactoring

Apply similar patterns to Project module:
- Create ProjectQueryDTO, ProjectVO, etc.
- Split ProjectList.vue into smaller components
- Create constants for project status, etc.

### 3. Duty Management Refactoring

Apply similar patterns to Duty module:
- Create DTOs for duty queries and updates
- Split large duty views into components
- Optimize API calls with permissions in list response

## Testing Guidelines

- Test all API endpoints with Postman or similar tools
- Test frontend component interactions
- Verify backward compatibility
- Check error handling and edge cases
- Validate permission checks work correctly

## Project Rules Reference

Always refer to `.trae/rules/project_rules.md` for:
- Technology stack constraints
- API calling patterns
- Cache management guidelines
- Code style conventions
