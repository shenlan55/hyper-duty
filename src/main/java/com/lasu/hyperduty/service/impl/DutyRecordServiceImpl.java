package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.dto.PageRequestDTO;
import com.lasu.hyperduty.dto.PageResponseDTO;
import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutyRecord;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.mapper.DutyRecordMapper;
import com.lasu.hyperduty.service.DutyAssignmentService;
import com.lasu.hyperduty.service.DutyRecordService;
import com.lasu.hyperduty.service.DutyScheduleService;
import com.lasu.hyperduty.service.SysEmployeeService;
import com.lasu.hyperduty.utils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DutyRecordServiceImpl extends CacheableServiceImpl<DutyRecordMapper, DutyRecord> implements DutyRecordService {

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private DutyScheduleService dutyScheduleService;

    @Override
    public List<SysEmployee> getAvailableSubstitutes(Long recordId) {
        DutyRecord record = getById(recordId);
        if (record == null) {
            return List.of();
        }
        
        DutyAssignment assignment = dutyAssignmentService.getById(record.getAssignmentId());
        if (assignment == null) {
            return List.of();
        }
        
        return getAvailableSubstitutes(record.getEmployeeId(), assignment.getDutyDate(), assignment.getDutyShift());
    }

    @Override
    public List<SysEmployee> getAvailableSubstitutes(Long employeeId, LocalDate dutyDate, Integer dutyShift) {
        SysEmployee currentEmployee = sysEmployeeService.getById(employeeId);
        if (currentEmployee == null) {
            return List.of();
        }

        List<SysEmployee> allEmployees = sysEmployeeService.getAllEmployees();

        List<Long> assignedEmployeeIds = dutyAssignmentService.lambdaQuery()
                .eq(DutyAssignment::getDutyDate, dutyDate)
                .eq(DutyAssignment::getDutyShift, dutyShift)
                .eq(DutyAssignment::getStatus, 1)
                .list()
                .stream()
                .map(DutyAssignment::getEmployeeId)
                .collect(Collectors.toList());

        return allEmployees.stream()
                .filter(emp -> !emp.getId().equals(employeeId))
                .filter(emp -> emp.getStatus() == 1)
                .filter(emp -> !assignedEmployeeIds.contains(emp.getId()))
                .sorted((e1, e2) -> {
                    if (e1.getDeptId().equals(currentEmployee.getDeptId()) && 
                        !e2.getDeptId().equals(currentEmployee.getDeptId())) {
                        return -1;
                    }
                    if (!e1.getDeptId().equals(currentEmployee.getDeptId()) && 
                        e2.getDeptId().equals(currentEmployee.getDeptId())) {
                        return 1;
                    }
                    return 0;
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<DutyRecord> getPendingApprovals(Long employeeId) {
        // 查询审批状态为待审批的加班记录
        List<DutyRecord> allPendingRecords = lambdaQuery()
                .eq(DutyRecord::getApprovalStatus, "待审批")
                .orderByDesc(DutyRecord::getCreateTime)
                .list();
        
        // 过滤出当前用户有权审批的加班记录
        List<DutyRecord> authorizedRecords = allPendingRecords.stream()
                .filter(record -> {
                    try {
                        // 根据assignmentId查询对应的值班安排
                        DutyAssignment assignment = dutyAssignmentService.getById(record.getAssignmentId());
                        if (assignment != null) {
                            // 根据值班安排的scheduleId查询对应的值班表
                            Long scheduleId = assignment.getScheduleId();
                            if (scheduleId != null) {
                                // 查询该值班表的值班长列表
                                List<Long> leaderIds = dutyScheduleService.getLeaderIdsByScheduleId(scheduleId);
                                // 检查当前用户是否是值班长
                                boolean isLeader = leaderIds.contains(employeeId);
                                return isLeader;
                            }
                        }
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        return authorizedRecords;
    }

    @Override
    protected void clearCache(DutyRecord entity) {
        // 由于不再使用缓存，此方法为空实现
    }

    @Override
    public boolean save(DutyRecord entity) {
        return super.save(entity);
    }

    @Override
    public boolean updateById(DutyRecord entity) {
        return super.updateById(entity);
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        return super.removeById(id);
    }

    @Override
    public PageResponseDTO<DutyRecord> page(PageRequestDTO pageRequestDTO, Map<String, Object> params) {
        // 创建MyBatis Plus的Page对象
        Page<DutyRecord> page = new Page<>(pageRequestDTO.getPageNum(), pageRequestDTO.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<DutyRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        // 从params中获取查询参数
        Long employeeId = params != null ? (Long) params.get("employeeId") : null;
        Long scheduleId = params != null ? (Long) params.get("scheduleId") : null;
        String keyword = params != null ? (String) params.get("keyword") : null;
        String date = params != null ? (String) params.get("date") : null;
        
        // 员工ID筛选
        if (employeeId != null) {
            queryWrapper.eq(DutyRecord::getEmployeeId, employeeId);
        }
        
        // 值班表ID筛选（需要关联查询）
        if (scheduleId != null) {
            // 这里需要使用子查询，因为DutyRecord表中没有直接的scheduleId字段
            // 而是通过assignmentId关联到DutyAssignment表，再关联到scheduleId
            queryWrapper.inSql(DutyRecord::getAssignmentId, 
                "SELECT id FROM duty_assignment WHERE schedule_id = " + scheduleId);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            // 可以根据需要添加搜索字段
            queryWrapper.like(DutyRecord::getCheckInRemark, keyword)
                .or().like(DutyRecord::getCheckOutRemark, keyword);
        }
        
        // 值班日期筛选（需要关联查询）
        if (date != null && !date.isEmpty()) {
            queryWrapper.inSql(DutyRecord::getAssignmentId, 
                "SELECT id FROM duty_assignment WHERE duty_date = '" + date + "'");
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(DutyRecord::getCreateTime);
        
        // 执行分页查询
        Page<DutyRecord> resultPage = baseMapper.selectPage(page, queryWrapper);
        
        // 转换为PageResponseDTO
        return PageResponseDTO.fromPage(resultPage);
    }

}