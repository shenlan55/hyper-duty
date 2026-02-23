package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.LeaveRequest;
import com.lasu.hyperduty.mapper.LeaveRequestMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyScheduleEmployeeService;
import com.lasu.hyperduty.service.LeaveRequestService;
import com.lasu.hyperduty.service.LeaveSubstituteService;
import com.lasu.hyperduty.service.SysEmployeeService;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private DutyScheduleEmployeeService dutyScheduleEmployeeService;

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private LeaveSubstituteService leaveSubstituteService;

    @Autowired
    private DutyShiftConfigService dutyShiftConfigService;

    @Override
    public String generateRequestNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "LV" + timestamp + random;
    }

    @Override
    public LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest) {
        leaveRequest.setRequestNo(generateRequestNo());
        leaveRequest.setApprovalStatus("pending");
        leaveRequest.setApprovalLevel(1);
        leaveRequest.setSubstituteStatus("pending");
        leaveRequest.setCreateTime(LocalDateTime.now());
        leaveRequest.setUpdateTime(LocalDateTime.now());
        
        if (leaveRequest.getScheduleId() != null) {
            List<com.lasu.hyperduty.entity.DutyScheduleEmployee> leaders = dutyScheduleEmployeeService.lambdaQuery()
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getScheduleId, leaveRequest.getScheduleId())
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getIsLeader, 1)
                    .list();
            if (!leaders.isEmpty()) {
                // 如果有多个值班长，选择第一个作为审批人
                leaveRequest.setCurrentApproverId(leaders.get(0).getEmployeeId());
            }
        }
        
        save(leaveRequest);
        return leaveRequest;
    }

    @Override
    public boolean approveLeaveRequest(Long requestId, Long approverId, String approvalStatus, String opinion, String scheduleAction, List<Map<String, Object>> substituteData, Boolean excludeSameDayShifts) {
        LeaveRequest request = getById(requestId);
        if (request == null) {
            return false;
        }

        // 检查请假申请是否处于待审批状态，如果不是，则表示已经被审批过了
        if (!"pending".equals(request.getApprovalStatus())) {
            return false;
        }

        // 权限控制：只有值班长才能审批请假申请
        // 1. 检查审批人是否是值班长
        boolean isLeader = false;
        if (request.getScheduleId() != null) {
            // 查询该值班表中的值班长
            List<com.lasu.hyperduty.entity.DutyScheduleEmployee> leaders = dutyScheduleEmployeeService.lambdaQuery()
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getScheduleId, request.getScheduleId())
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getIsLeader, 1)
                    .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getEmployeeId, approverId)
                    .list();
            isLeader = !leaders.isEmpty();
        }
        
        // 只有值班长可以审批请假
        if (!isLeader) {
            return false;
        }
        
        // 2. 检查当前审批人是否是请假申请的审批人（值班长）
        if (request.getCurrentApproverId() != null && !approverId.equals(request.getCurrentApproverId())) {
            return false;
        }

        if ("approved".equals(approvalStatus) && "check".equals(scheduleAction)) {
            // 只检查排班状态，不阻止审批
            Map<String, Object> scheduleCheck = checkEmployeeSchedule(request.getEmployeeId(), request.getStartDate().toString(), request.getEndDate().toString(), request.getScheduleId());
        }

        request.setApprovalStatus(approvalStatus);
        request.setCurrentApproverId(approverId);
        request.setUpdateTime(LocalDateTime.now());

        if ("rejected".equals(approvalStatus)) {
            request.setRejectReason(opinion);
        }

        if ("approved".equals(approvalStatus) && "substitute".equals(scheduleAction)) {
            // 处理顶岗人员排班
            if (substituteData != null && !substituteData.isEmpty()) {
                try {
                    for (Map<String, Object> data : substituteData) {
                        String date = (String) data.get("date");
                        if (date == null) continue;
                        
                        Object shiftIdObj = data.get("shiftId");
                        if (shiftIdObj == null) continue;
                        Long shiftId = null;
                        if (shiftIdObj instanceof Number) {
                            shiftId = ((Number) shiftIdObj).longValue();
                        } else if (shiftIdObj instanceof String) {
                            try {
                                shiftId = Long.parseLong((String) shiftIdObj);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                        }
                        if (shiftId == null) continue;
                        
                        Object substituteEmployeeIdObj = data.get("substituteEmployeeId");
                        if (substituteEmployeeIdObj == null) continue;
                        Long substituteEmployeeId = null;
                        if (substituteEmployeeIdObj instanceof Number) {
                            substituteEmployeeId = ((Number) substituteEmployeeIdObj).longValue();
                        } else if (substituteEmployeeIdObj instanceof String) {
                            try {
                                substituteEmployeeId = Long.parseLong((String) substituteEmployeeIdObj);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                        }
                        if (substituteEmployeeId == null) continue;
                        
                        try {
                            LocalDate dutyDate = LocalDate.parse(date);
                            // 创建局部变量存储shiftId，使其成为effectively final
                            final Long finalShiftId = shiftId;
                            // 检查是否已有排班
                            List<DutyAssignment> existingAssignments = dutyAssignmentService.lambdaQuery()
                                    .eq(DutyAssignment::getScheduleId, request.getScheduleId())
                                    .eq(DutyAssignment::getEmployeeId, request.getEmployeeId())
                                    .eq(DutyAssignment::getDutyDate, dutyDate)
                                    .and(wrapper -> wrapper
                                            .eq(DutyAssignment::getShiftConfigId, finalShiftId)
                                            .or()
                                            .eq(DutyAssignment::getDutyShift, finalShiftId.intValue())
                                    )
                                    .list();
                            
                            if (!existingAssignments.isEmpty()) {
                                // 已排班，替换为顶岗人员
                                for (DutyAssignment assignment : existingAssignments) {
                                    assignment.setEmployeeId(substituteEmployeeId);
                                    // 确保顶岗人显示为有效
                                    assignment.setStatus(1);
                                    dutyAssignmentService.updateById(assignment);
                                }
                            } else {
                                // 未排班，不创建新的排班记录
                                // 根据用户要求：没有安排排班时，不存在替换的情况，顶岗人就不用加到值班安排
                            }
                        } catch (Exception e) {
                            // 日期解析或其他异常，跳过当前记录
                            continue;
                        }
                    }
                    
                    // 保存顶岗信息到数据库
                    leaveSubstituteService.saveSubstitutes(requestId, request.getEmployeeId(), substituteData);
                } catch (Exception e) {
                    // 捕获所有异常，确保审批流程不会中断
                    e.printStackTrace();
                }
            }
            
            request.setScheduleCompleted(1);
            request.setScheduleCompletedTime(LocalDateTime.now());
            request.setScheduleCompletedBy(approverId);
        }

        return updateById(request);
    }

     @Override
    public List<Object> getApprovalRecords(Long requestId) {
        return lambdaQuery()
                .eq(LeaveRequest::getId, requestId)
                .isNotNull(LeaveRequest::getCurrentApproverId)
                .isNotNull(LeaveRequest::getApprovalStatus)
                .orderByAsc(LeaveRequest::getUpdateTime)
                .list()
                .stream()
                .map(req -> {
                    java.util.Map<String, Object> record = new java.util.HashMap<>();
                    record.put("approverId", req.getCurrentApproverId());
                    record.put("approverName", getApproverName(req.getCurrentApproverId()));
                    record.put("approvalStatus", req.getApprovalStatus());
                    record.put("approvalOpinion", req.getApprovalOpinion());
                    record.put("rejectReason", req.getRejectReason());
                    record.put("createTime", req.getUpdateTime());
                    return record;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Map<String, Object> checkEmployeeSchedule(Long employeeId, String startDate, String endDate, Long scheduleId) {
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);

        LambdaQueryWrapper<DutyAssignment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DutyAssignment::getEmployeeId, employeeId)
                .ge(DutyAssignment::getDutyDate, start)
                .le(DutyAssignment::getDutyDate, end);

        // 如果提供了scheduleId，只查询该值班表的排班
        if (scheduleId != null) {
            queryWrapper.eq(DutyAssignment::getScheduleId, scheduleId);
        }

        List<DutyAssignment> assignments = dutyAssignmentService.list(queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("hasSchedule", !assignments.isEmpty());
        result.put("assignments", assignments);
        return result;
    }

    private String getApproverName(Long approverId) {
        if (approverId == null) {
            return "系统";
        }
        return approverId.toString();
    }

    @Override
    public List<LeaveRequest> getPendingApprovals(Long approverId) {
        // 查询当前审批人负责审批的请假申请（只有值班长才能审批）
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("approval_status", "pending");
        // 添加条件：当前审批人是请假申请的审批人（值班长）
        queryWrapper.eq("current_approver_id", approverId);
        queryWrapper.orderByDesc("create_time");
        List<LeaveRequest> result = baseMapper.selectList(queryWrapper);
        System.out.println("Pending approvals count: " + result.size());
        for (LeaveRequest request : result) {
            System.out.println("Request ID: " + request.getId() + ", Status: " + request.getApprovalStatus());
        }
        return result;
    }

    @Override
    public List<LeaveRequest> getMyLeaveRequests(Long employeeId) {
        return lambdaQuery()
                .eq(LeaveRequest::getEmployeeId, employeeId)
                .orderByDesc(LeaveRequest::getCreateTime)
                .list();
    }

    @Override
    public List<LeaveRequest> getPendingApprovalsByScheduleId(Long scheduleId) {
        return lambdaQuery()
                .eq(LeaveRequest::getScheduleId, scheduleId)
                .eq(LeaveRequest::getApprovalStatus, "pending")
                .orderByDesc(LeaveRequest::getCreateTime)
                .list();
    }

    @Override
    public List<LeaveRequest> getApprovedApprovals(Long approverId) {
        // 查询当前审批人负责审批的请假申请，或者是发起人自己的请假申请
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("approval_status", "approved", "rejected");
        // 添加条件：当前审批人是请假申请的审批人，或者当前审批人是请假申请的发起人
        queryWrapper.and(wrapper -> wrapper
                .eq("current_approver_id", approverId)
                .or()
                .eq("employee_id", approverId)
        );
        queryWrapper.orderByDesc("update_time");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<LeaveRequest> getApprovedApprovalsByScheduleId(Long scheduleId) {
        return lambdaQuery()
                .eq(LeaveRequest::getScheduleId, scheduleId)
                .in(LeaveRequest::getApprovalStatus, "approved", "rejected")
                .orderByDesc(LeaveRequest::getUpdateTime)
                .list();
    }

    @Override
    public boolean confirmScheduleCompletion(Long requestId, Long approverId) {
        LeaveRequest request = getById(requestId);
        if (request == null) {
            return false;
        }

        request.setScheduleCompleted(1);
        request.setScheduleCompletedTime(LocalDateTime.now());
        request.setScheduleCompletedBy(approverId);
        request.setApprovalStatus("approved");
        request.setUpdateTime(LocalDateTime.now());

        return updateById(request);
    }

    @Override
    public Map<Long, Map<String, Map<String, List<Long>>>> getEmployeeLeaveInfo(List<Long> employeeIds, String startDate, String endDate) {
        Map<Long, Map<String, Map<String, List<Long>>>> leaveInfo = new java.util.HashMap<>();
        
        if (employeeIds == null || employeeIds.isEmpty()) {
            return leaveInfo;
        }
        
        // 将字符串日期转换为LocalDate
        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);
        
        // 查询员工在指定日期范围内的请假记录
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("employee_id", employeeIds);
        // 查询与指定日期范围有重叠的请假记录
        queryWrapper.le("start_date", endLocalDate); // 请假开始日期 <= 范围结束日期
        queryWrapper.ge("end_date", startLocalDate); // 请假结束日期 >= 范围开始日期
        queryWrapper.eq("approval_status", "approved"); // 只考虑已通过的请假
        
        List<LeaveRequest> leaveRequests = this.list(queryWrapper);
        
        // 处理请假记录，提取请假日期、值班表ID和班次配置ID列表
        for (LeaveRequest request : leaveRequests) {
            Long employeeId = request.getEmployeeId();
            Long scheduleId = request.getScheduleId();
            String shiftConfigIdsStr = request.getShiftConfigIds();
            
            // 解析班次配置ID列表
            List<Long> shiftConfigIds = new java.util.ArrayList<>();
            if (shiftConfigIdsStr != null && !shiftConfigIdsStr.isEmpty()) {
                String[] ids = shiftConfigIdsStr.split(",");
                for (String idStr : ids) {
                    try {
                        shiftConfigIds.add(Long.parseLong(idStr.trim()));
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的ID
                        System.err.println("班次配置ID格式错误: " + idStr);
                    }
                }
            }
            
            // 如果没有班次配置ID，尝试使用单个班次配置ID
            if (shiftConfigIds.isEmpty() && request.getShiftConfigId() != null) {
                shiftConfigIds.add(request.getShiftConfigId());
            }
            
            java.time.LocalDate start = request.getStartDate();
            java.time.LocalDate end = request.getEndDate();
            
            // 跳过空日期的请假记录
            if (start == null || end == null) {
                continue;
            }
            
            // 遍历请假期间的所有日期
            java.time.LocalDate current = start;
            while (!current.isAfter(end)) {
                String dateStr = current.toString();
                
                // 初始化嵌套映射结构
                Map<String, Map<String, List<Long>>> employeeLeaveMap = leaveInfo.computeIfAbsent(employeeId, k -> new java.util.HashMap<>());
                Map<String, List<Long>> dateLeaveMap = employeeLeaveMap.computeIfAbsent(dateStr, k -> new java.util.HashMap<>());
                
                // 存储值班表ID和班次配置ID列表
                String scheduleIdStr = scheduleId != null ? scheduleId.toString() : "null";
                dateLeaveMap.put(scheduleIdStr, shiftConfigIds);
                
                current = current.plusDays(1);
            }
        }
        
        return leaveInfo;
    }

    @Override
    public IPage<LeaveRequest> getMyLeaveRequestsPage(Long employeeId, Integer page, Integer size, Integer leaveType, String approvalStatus, String startDate, String endDate, String searchQuery) {
        IPage<LeaveRequest> iPage = new Page<>(page, size);
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：员工ID
        queryWrapper.eq("employee_id", employeeId);
        
        // 可选条件：请假类型
        if (leaveType != null) {
            queryWrapper.eq("leave_type", leaveType);
        }
        
        // 可选条件：审批状态
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            queryWrapper.eq("approval_status", approvalStatus);
        }
        
        // 可选条件：开始日期
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("start_date", LocalDate.parse(startDate));
        }
        
        // 可选条件：结束日期
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("end_date", LocalDate.parse(endDate));
        }
        
        // 可选条件：搜索关键词
        if (searchQuery != null && !searchQuery.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("request_no", searchQuery)
                    .or().like("reason", searchQuery)
            );
        }
        
        // 排序：创建时间倒序
        queryWrapper.orderByDesc("create_time");
        
        return this.page(iPage, queryWrapper);
    }

    @Override
    public IPage<LeaveRequest> getPendingApprovalsPage(Long approverId, Integer page, Integer size, Long scheduleId, Integer leaveType, String startDate, String endDate, String searchQuery) {
        IPage<LeaveRequest> iPage = new Page<>(page, size);
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：审批状态为待审批
        queryWrapper.eq("approval_status", "pending");
        // 添加条件：当前审批人是请假申请的审批人（值班长）
        queryWrapper.eq("current_approver_id", approverId);
        
        // 可选条件：值班表ID
        if (scheduleId != null) {
            queryWrapper.eq("schedule_id", scheduleId);
        }
        
        // 可选条件：请假类型
        if (leaveType != null) {
            queryWrapper.eq("leave_type", leaveType);
        }
        
        // 可选条件：开始日期
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("start_date", LocalDate.parse(startDate));
        }
        
        // 可选条件：结束日期
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("end_date", LocalDate.parse(endDate));
        }
        
        // 可选条件：搜索关键词
        if (searchQuery != null && !searchQuery.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("request_no", searchQuery)
                    .or().like("reason", searchQuery)
                    .or().like("employee_id", searchQuery)
            );
        }
        
        // 排序：创建时间倒序
        queryWrapper.orderByDesc("create_time");
        
        return this.page(iPage, queryWrapper);
    }

    @Override
    public IPage<LeaveRequest> getApprovedApprovalsPage(Long approverId, Integer page, Integer size, Long scheduleId, Integer leaveType, String approvalStatus, String startDate, String endDate, String searchQuery) {
        IPage<LeaveRequest> iPage = new Page<>(page, size);
        QueryWrapper<LeaveRequest> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：审批状态为已审批或已拒绝
        queryWrapper.in("approval_status", "approved", "rejected");
        // 添加条件：当前审批人是请假申请的审批人，或者当前审批人是请假申请的发起人
        queryWrapper.and(wrapper -> wrapper
                .eq("current_approver_id", approverId)
                .or()
                .eq("employee_id", approverId)
        );
        
        // 可选条件：值班表ID
        if (scheduleId != null) {
            queryWrapper.eq("schedule_id", scheduleId);
        }
        
        // 可选条件：请假类型
        if (leaveType != null) {
            queryWrapper.eq("leave_type", leaveType);
        }
        
        // 可选条件：审批状态
        if (approvalStatus != null && !approvalStatus.isEmpty()) {
            queryWrapper.eq("approval_status", approvalStatus);
        }
        
        // 可选条件：开始日期
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("start_date", LocalDate.parse(startDate));
        }
        
        // 可选条件：结束日期
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("end_date", LocalDate.parse(endDate));
        }
        
        // 可选条件：搜索关键词
        if (searchQuery != null && !searchQuery.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("request_no", searchQuery)
                    .or().like("reason", searchQuery)
                    .or().like("employee_id", searchQuery)
            );
        }
        
        // 排序：更新时间倒序
        queryWrapper.orderByDesc("update_time");
        
        return this.page(iPage, queryWrapper);
    }

    @Override
    public List<com.lasu.hyperduty.entity.SysEmployee> getAvailableSubstitutes(Long scheduleId, String startDate, String endDate, Long leaveEmployeeId, Boolean excludeSameDayShifts, Long shiftId) {
        // 获取值班表中的所有员工
        List<com.lasu.hyperduty.entity.DutyScheduleEmployee> scheduleEmployees = dutyScheduleEmployeeService.lambdaQuery()
                .eq(com.lasu.hyperduty.entity.DutyScheduleEmployee::getScheduleId, scheduleId)
                .list();
        
        // 提取员工ID列表
        List<Long> employeeIds = scheduleEmployees.stream()
                .map(com.lasu.hyperduty.entity.DutyScheduleEmployee::getEmployeeId)
                .collect(java.util.stream.Collectors.toList());
        
        // 排除请假的员工
        if (employeeIds.contains(leaveEmployeeId)) {
            employeeIds.remove(leaveEmployeeId);
        }
        
        // 获取请假期间也请假的员工
        Map<Long, Map<String, Map<String, List<Long>>>> leaveInfo = getEmployeeLeaveInfo(employeeIds, startDate, endDate);
        List<Long> leaveDuringEmployees = leaveInfo.keySet().stream().collect(java.util.stream.Collectors.toList());
        
        // 排除请假期间也请假的员工
        employeeIds.removeAll(leaveDuringEmployees);
        
        // 如果需要排除当日已有班次的人员
        if (excludeSameDayShifts != null && excludeSameDayShifts && shiftId != null) {
            LocalDate dutyDate = LocalDate.parse(startDate);
            
            // 查询当日已有该班次的人员
            List<com.lasu.hyperduty.entity.DutyAssignment> sameDayAssignments = dutyAssignmentService.lambdaQuery()
                    .eq(com.lasu.hyperduty.entity.DutyAssignment::getScheduleId, scheduleId)
                    .eq(com.lasu.hyperduty.entity.DutyAssignment::getDutyDate, dutyDate)
                    .and(wrapper -> wrapper
                            .eq(com.lasu.hyperduty.entity.DutyAssignment::getShiftConfigId, shiftId)
                            .or()
                            .eq(com.lasu.hyperduty.entity.DutyAssignment::getDutyShift, shiftId.intValue())
                    )
                    .list();
            
            // 提取当日已有该班次的人员ID
            List<Long> sameDayEmployees = sameDayAssignments.stream()
                    .map(com.lasu.hyperduty.entity.DutyAssignment::getEmployeeId)
                    .collect(java.util.stream.Collectors.toList());
            
            // 排除当日已有该班次的人员
            employeeIds.removeAll(sameDayEmployees);
        }
        
        // 根据员工ID列表查询员工信息
        if (employeeIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<com.lasu.hyperduty.entity.SysEmployee> employees = sysEmployeeService.listByIds(employeeIds);
        
        // 为每个员工添加当月工时信息
        LocalDate currentDate = LocalDate.now();
        String monthStart = currentDate.withDayOfMonth(1).toString();
        String monthEnd = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString();
        
        for (com.lasu.hyperduty.entity.SysEmployee employee : employees) {
            // 计算当月校准工时：计划工时 - 请假工时
            int monthlyWorkHours = calculateMonthlyWorkHours(employee.getId(), monthStart, monthEnd);
            employee.setMonthlyWorkHours(monthlyWorkHours);
        }
        
        return employees;
    }
    
    /**
     * 计算员工当月工时
     * @param employeeId 员工ID
     * @param monthStart 月份开始日期
     * @param monthEnd 月份结束日期
     * @return 当月工时
     */
    private int calculateMonthlyWorkHours(Long employeeId, String monthStart, String monthEnd) {
        // 1. 获取员工实际排班工时
        int plannedWorkHours = getEmployeeScheduledWorkHours(employeeId, monthStart, monthEnd);
        
        // 2. 计算员工当月请假工时
        int leaveHours = calculateLeaveHours(employeeId, monthStart, monthEnd);
        
        // 3. 计算校准工时：计划工时 - 请假工时
        int calibratedWorkHours = plannedWorkHours - leaveHours;
        
        // 确保工时不为负数
        return Math.max(0, calibratedWorkHours);
    }
    
    /**
     * 获取员工当月实际排班工时
     * @param employeeId 员工ID
     * @param monthStart 月份开始日期
     * @param monthEnd 月份结束日期
     * @return 当月排班工时
     */
    private int getEmployeeScheduledWorkHours(Long employeeId, String monthStart, String monthEnd) {
        // 查询员工当月的所有排班记录
        List<com.lasu.hyperduty.entity.DutyAssignment> assignments = dutyAssignmentService.lambdaQuery()
                .eq(com.lasu.hyperduty.entity.DutyAssignment::getEmployeeId, employeeId)
                .ge(com.lasu.hyperduty.entity.DutyAssignment::getDutyDate, LocalDate.parse(monthStart))
                .le(com.lasu.hyperduty.entity.DutyAssignment::getDutyDate, LocalDate.parse(monthEnd))
                .eq(com.lasu.hyperduty.entity.DutyAssignment::getStatus, 1)
                .list();
        
        // 计算总排班工时
        int totalHours = 0;
        for (com.lasu.hyperduty.entity.DutyAssignment assignment : assignments) {
            if (assignment.getShiftConfigId() != null) {
                // 根据班次配置计算工时
                com.lasu.hyperduty.entity.DutyShiftConfig shiftConfig = dutyShiftConfigService.getById(assignment.getShiftConfigId());
                if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
                    totalHours += shiftConfig.getDurationHours().intValue();
                } else {
                    // 默认8小时
                    totalHours += 8;
                }
            } else {
                // 默认8小时
                totalHours += 8;
            }
        }
        
        return totalHours;
    }
    
    /**
     * 计算员工当月请假工时
     * @param employeeId 员工ID
     * @param monthStart 月份开始日期
     * @param monthEnd 月份结束日期
     * @return 请假工时
     */
    private int calculateLeaveHours(Long employeeId, String monthStart, String monthEnd) {
        // 查询员工当月的请假记录
        List<LeaveRequest> leaveRequests = lambdaQuery()
                .eq(LeaveRequest::getEmployeeId, employeeId)
                .eq(LeaveRequest::getApprovalStatus, "approved")
                .ge(LeaveRequest::getStartDate, LocalDate.parse(monthStart))
                .le(LeaveRequest::getEndDate, LocalDate.parse(monthEnd))
                .list();
        
        // 计算请假总工时
        int totalLeaveHours = 0;
        for (LeaveRequest request : leaveRequests) {
            if (request.getTotalHours() != null) {
                totalLeaveHours += request.getTotalHours().intValue();
            }
        }
        
        return totalLeaveHours;
    }

    @Override
    public List<Map<String, Object>> autoSelectSubstitutes(Long requestId, List<Map<String, Object>> substituteData) {
        // 获取请假申请信息
        LeaveRequest leaveRequest = getById(requestId);
        if (leaveRequest == null) {
            return substituteData;
        }
        
        Long scheduleId = leaveRequest.getScheduleId();
        Long leaveEmployeeId = leaveRequest.getEmployeeId();
        
        // 初始化动态工时状态，记录每个员工的当前累计工时
        Map<Long, Integer> dynamicWorkHours = new HashMap<>();
        
        // 处理每个顶岗班次
        for (Map<String, Object> item : substituteData) {
            String date = (String) item.get("date");
            Long shiftId = null;
            Object shiftIdObj = item.get("shiftId");
            if (shiftIdObj != null) {
                if (shiftIdObj instanceof Number) {
                    shiftId = ((Number) shiftIdObj).longValue();
                } else if (shiftIdObj instanceof String) {
                    try {
                        shiftId = Long.parseLong((String) shiftIdObj);
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的班次ID
                    }
                }
            }
            
            if (date != null && shiftId != null) {
                // 获取该班次的时长
                int shiftHours = getShiftDurationHours(shiftId);
                
                // 获取该日期班次的可用顶岗人员
                List<com.lasu.hyperduty.entity.SysEmployee> availableSubstitutes = getAvailableSubstitutes(
                        scheduleId, date, date, leaveEmployeeId, true, shiftId
                );
                
                if (!availableSubstitutes.isEmpty()) {
                    // 计算每个可用人员的动态工时（基础工时 + 已分配的工时）
                    List<com.lasu.hyperduty.entity.SysEmployee> substitutesWithDynamicHours = availableSubstitutes.stream()
                            .map(sub -> {
                                int baseHours = sub.getMonthlyWorkHours() != null ? sub.getMonthlyWorkHours() : 0;
                                int allocatedHours = dynamicWorkHours.getOrDefault(sub.getId(), 0);
                                // 临时设置动态工时用于排序
                                sub.setMonthlyWorkHours(baseHours + allocatedHours);
                                return sub;
                            })
                            .collect(Collectors.toList());
                    
                    // 按动态工时排序，选择工时最少的
                    substitutesWithDynamicHours.sort(Comparator.comparingInt(e -> e.getMonthlyWorkHours() != null ? e.getMonthlyWorkHours() : 0));
                    com.lasu.hyperduty.entity.SysEmployee selectedSubstitute = substitutesWithDynamicHours.get(0);
                    
                    // 分配顶岗人员
                    item.put("substituteEmployeeId", selectedSubstitute.getId());
                    item.put("substituteEmployeeName", selectedSubstitute.getEmployeeName());
                    
                    // 更新动态工时
                    dynamicWorkHours.put(selectedSubstitute.getId(), 
                            dynamicWorkHours.getOrDefault(selectedSubstitute.getId(), 0) + shiftHours);
                }
            }
        }
        
        return substituteData;
    }
    
    /**
     * 获取班次的时长（小时）
     * @param shiftId 班次ID
     * @return 班次时长
     */
    private int getShiftDurationHours(Long shiftId) {
        // 从数据库中获取班次配置
        com.lasu.hyperduty.entity.DutyShiftConfig shiftConfig = dutyShiftConfigService.getById(shiftId);
        if (shiftConfig != null && shiftConfig.getDurationHours() != null) {
            return shiftConfig.getDurationHours().intValue();
        }
        // 默认8小时
        return 8;
    }
}
