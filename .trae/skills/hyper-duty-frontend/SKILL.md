---
name: "hyper-duty-frontend"
description: "Develops and maintains frontend components for the Hyper Duty system using Vue 3, Element Plus, and Axios. Invoke when working on frontend features, UI bugs, or Vue component development."
---

# Hyper Duty Frontend Developer

This skill specializes in developing and maintaining frontend components for the Hyper Duty system, focusing on Vue 3, Element Plus, and Axios integration.

## Core Responsibilities

- **Component Development**: Create and maintain Vue 3 components using Composition API
- **API Integration**: Implement Axios API calls with proper response handling
- **UI Implementation**: Build responsive interfaces using Element Plus
- **State Management**: Manage application state using Pinia
- **Route Configuration**: Set up and maintain Vue Router configurations
- **Form Validation**: Implement client-side form validation
- **Error Handling**: Create unified error handling mechanisms
- **Performance Optimization**: Optimize frontend performance and loading times
- **Component Reusability**: Use and extend BaseTable and VirtualList components for consistent UI
- **Security**: Implement XSS protection using xssUtil.js
- **Code Quality**: Ensure consistent code style and best practices

## Key Files and Directories

### Core Configuration
- `frontend/src/main.js` - Application entry point
- `frontend/src/router/index.js` - Route configurations
- `frontend/src/stores/index.js` - Pinia store setup
- `frontend/src/utils/request.js` - Axios interceptor configuration
- `frontend/src/utils/xssUtil.js` - XSS protection utilities

### Reusable Components
- `frontend/src/components/BaseTable.vue` - Unified table component with pagination and sorting
- `frontend/src/components/VirtualList.vue` - Virtual scroll component for long lists

### View Components
- `frontend/src/views/` - Main page components
  - `Login.vue` - Authentication page
  - `Dashboard.vue` - Main dashboard
  - `Employee.vue` - Employee management
  - `Dept.vue` - Department management
  - `Role.vue` - Role management
  - `Menu.vue` - Menu management

### Duty Management Components
- `frontend/src/views/duty/` - Duty management pages
  - `DutyAssignment.vue` - Duty scheduling
  - `LeaveRequest.vue` - Leave requests
  - `LeaveApproval.vue` - Leave approval
  - `SwapRequest.vue` - Shift swaps
  - `Statistics.vue` - Duty statistics
  - `OperationLog.vue` - Operation logs

### API Services
- `frontend/src/api/` - API service modules
  - `auth.js` - Authentication APIs
  - `employee.js` - Employee APIs
  - `dept.js` - Department APIs
  - `role.js` - Role APIs
  - `menu.js` - Menu APIs
  - `duty/` - Duty management APIs

## Development Guidelines

1. **API Response Handling**: Use the simplified pattern - successful responses return `data.data` directly
   ```javascript
   const data = await apiFunction();
   data.value = data || [];
   ```

2. **Component Structure**: Follow Vue 3 Composition API best practices
   - Use `<script setup>` for simplicity
   - Organize code by functionality
   - Use reactive state appropriately

3. **Form Handling**: Use Element Plus form components with proper validation
   - Implement real-time validation
   - Provide clear error messages
   - Handle form submission states

4. **State Management**: Use Pinia for global state
   - Keep components as stateless as possible
   - Use stores for shared data
   - Implement proper store structure

5. **Error Handling**: Implement unified error handling
   - Use Axios interceptors for API errors
   - Display user-friendly error messages
   - Log errors for debugging

## Common Issues and Solutions

1. **Data Display Problems**: Ensure API calls follow the simplified pattern
2. **Form Validation Errors**: Check Element Plus form rules and binding
3. **Route Navigation Issues**: Verify Vue Router configurations and guards
4. **State Management Bugs**: Check Pinia store mutations and actions
5. **Performance Issues**: Optimize component rendering and API calls

## Testing Guidelines

- Test component rendering with different data states
- Verify form validation and submission
- Test API integration with mock data
- Ensure responsive design works across devices
- Test error handling scenarios

## Best Practices

- **Code Organization**: Keep components small and focused
- **Reusability**: Create reusable components for common UI elements
- **Documentation**: Add comments for complex logic
- **Consistency**: Follow project coding standards
- **Accessibility**: Ensure UI elements are accessible

## Example Workflow

1. **Component Creation**: Create new Vue component with proper structure
2. **API Integration**: Implement API calls using the simplified pattern
3. **UI Implementation**: Build interface using Element Plus
4. **State Management**: Set up Pinia store if needed
5. **Testing**: Test component functionality and edge cases
6. **Optimization**: Optimize performance and user experience