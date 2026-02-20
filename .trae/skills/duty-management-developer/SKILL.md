---
name: "duty-management-developer"
description: "Develops, debugs, and documents the duty management module, including shift scheduling, leave management, shift swap, and statistics reporting. Invoke when working on core duty management functionality."
---

# Duty Management Developer

This skill specializes in developing and maintaining the duty management module of the Hyper Duty system, including shift scheduling, leave management, shift swap, and statistics reporting.

## Core Responsibilities

- **Shift Scheduling**: Implement and optimize automatic and manual shift scheduling functionality
- **Leave Management**: Develop leave request submission, approval, and substitute assignment features
- **Shift Swap**: Implement shift swap request and confirmation workflows
- **Statistics Reporting**: Create and maintain duty statistics and overtime calculations
- **Holiday Management**: Handle holiday recognition in scheduling logic
- **Batch Operations**: Support batch scheduling and mass updates
- **Cache Management**: Implement and maintain Redis caching for duty-related hot data

## Key Files and Components

### Frontend Components
- `frontend/src/views/duty/DutyAssignment.vue` - Duty scheduling and assignment
- `frontend/src/views/duty/LeaveRequest.vue` - Leave request submission
- `frontend/src/views/duty/LeaveApproval.vue` - Leave request approval
- `frontend/src/views/duty/SwapRequest.vue` - Shift swap management
- `frontend/src/views/duty/Statistics.vue` - Duty statistics and reporting
- `frontend/src/views/duty/OperationLog.vue` - Operation log management

### Backend Services
- `src/main/java/com/lasu/hyperduty/service/DutyAssignmentService.java` - Duty assignment management
- `src/main/java/com/lasu/hyperduty/service/LeaveRequestService.java` - Leave request processing
- `src/main/java/com/lasu/hyperduty/service/SwapRequestService.java` - Shift swap management
- `src/main/java/com/lasu/hyperduty/service/DutyStatisticsService.java` - Duty statistics
- `src/main/java/com/lasu/hyperduty/service/AutoScheduleService.java` - Automatic scheduling
- `src/main/java/com/lasu/hyperduty/service/DutyHolidayService.java` - Holiday management with caching
- `src/main/java/com/lasu/hyperduty/service/DutyRecordService.java` - Overtime records with caching
- `src/main/java/com/lasu/hyperduty/service/DutyScheduleModeService.java` - Schedule modes with caching
- `src/main/java/com/lasu/hyperduty/service/DutyShiftConfigService.java` - Shift configurations with caching

### Cache Management
- `src/main/java/com/lasu/hyperduty/service/impl/CacheableServiceImpl.java` - Base service class for cache management
- `src/main/java/com/lasu/hyperduty/utils/CacheUtil.java` - Utility class for cache operations

### Controllers
- `src/main/java/com/lasu/hyperduty/controller/DutyAssignmentController.java`
- `src/main/java/com/lasu/hyperduty/controller/LeaveRequestController.java`
- `src/main/java/com/lasu/hyperduty/controller/SwapRequestController.java`
- `src/main/java/com/lasu/hyperduty/controller/DutyStatisticsController.java`
- `src/main/java/com/lasu/hyperduty/controller/AutoScheduleController.java`

## Development Guidelines

1. **API Response Handling**: Use the response interceptor pattern - successful responses return `data.data` directly
2. **Error Handling**: Implement unified error handling in both frontend and backend
3. **Data Validation**: Validate all user inputs and API parameters
4. **Performance Optimization**: Optimize database queries and avoid N+1 problems
5. **Security**: Follow Spring Security best practices and JWT token management
6. **Cache Management**:
   - **Cache Key Design**: Use `duty::{key}_{params}` format for duty-related cache keys
   - **Cache Clear Mechanisms**:
     - Simple scenarios: Use @CacheEvict annotation
     - Complex scenarios: Extend CacheableServiceImpl and implement clearCache method
     - Special scenarios: Use CacheUtil utility class
   - **Cache Consistency**: Always clear related caches after create/update/delete operations
   - **Cache Monitoring**: Regularly check Redis cache usage to avoid cache bloat

## Common Issues and Solutions

1. **Data Display Problems**: Ensure API calls follow the simplified pattern without checking `response.code`
2. **Scheduling Conflicts**: Implement proper conflict detection in scheduling algorithms
3. **Leave Substitute Assignment**: Ensure proper substitute assignment when employees take leave
4. **Shift Swap Approval**: Implement proper approval workflows for shift swaps
5. **Statistics Calculations**: Ensure accurate overtime and compensatory time calculations

## Testing Guidelines

- Test all scheduling scenarios including holidays and weekends
- Verify leave request and approval workflows
- Test shift swap functionality with different user roles
- Validate statistics calculations with various duty scenarios
- Ensure batch operations work correctly with large datasets