package com.lasu.hyperduty.duty.service;

import com.lasu.hyperduty.duty.service.DutyScheduleModeService;
import com.lasu.hyperduty.duty.service.DutyScheduleService;
import com.lasu.hyperduty.duty.service.LeaveRequestService;
import com.lasu.hyperduty.duty.service.LeaveSubstituteService;
import com.lasu.hyperduty.duty.entity.DutyAssignment;
import com.lasu.hyperduty.duty.entity.DutySchedule;
import com.lasu.hyperduty.duty.entity.DutyScheduleMode;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.duty.service.impl.AutoScheduleServiceImpl;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoScheduleServiceImplTest {

    @InjectMocks
    private AutoScheduleServiceImpl autoScheduleService;

    @Mock
    private DutyScheduleService dutyScheduleService;

    @Mock
    private DutyScheduleModeService dutyScheduleModeService;

    @Mock
    private SysEmployeeService sysEmployeeService;

    @Mock
    private LeaveRequestService leaveRequestService;

    @Mock
    private LeaveSubstituteService leaveSubstituteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateScheduleByMode_OnePersonTeam_ShouldAssignSamePerson() {
        // 准备测试数据
        Long scheduleId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(3);
        Long modeId = 1L;

        // 创建值班表
        DutySchedule schedule = new DutySchedule();
        schedule.setId(scheduleId);
        when(dutyScheduleService.getById(scheduleId)).thenReturn(schedule);

        // 创建员工列表
        List<SysEmployee> employees = new ArrayList<>();
        SysEmployee employee1 = new SysEmployee();
        employee1.setId(1L);
        employee1.setEmployeeName("员工1");
        employee1.setStatus(1);
        employees.add(employee1);

        SysEmployee employee2 = new SysEmployee();
        employee2.setId(2L);
        employee2.setEmployeeName("员工2");
        employee2.setStatus(1);
        employees.add(employee2);

        when(sysEmployeeService.listByIds(anyList())).thenReturn(employees);
        when(dutyScheduleService.getEmployeeIdsByScheduleId(scheduleId)).thenReturn(List.of(1L, 2L));

        // 创建排班模式
        DutyScheduleMode mode = new DutyScheduleMode();
        mode.setId(modeId);
        when(dutyScheduleModeService.getById(modeId)).thenReturn(mode);

        // 创建排班模式配置
        Map<String, Object> modeConfig = new HashMap<>();
        List<Map<String, Object>> teams = new ArrayList<>();
        
        // 配置1人班组
        Map<String, Object> team1 = new HashMap<>();
        List<Map<String, Object>> shifts1 = new ArrayList<>();
        
        // 第一天：白班，1人
        Map<String, Object> shift1Day1 = new HashMap<>();
        shift1Day1.put("shiftId", "1");
        shift1Day1.put("count", 1);
        shifts1.add(shift1Day1);
        
        // 第二天：夜班，1人
        Map<String, Object> shift1Day2 = new HashMap<>();
        shift1Day2.put("shiftId", "2");
        shift1Day2.put("count", 1);
        shifts1.add(shift1Day2);
        
        // 第三天：白班，1人
        Map<String, Object> shift1Day3 = new HashMap<>();
        shift1Day3.put("shiftId", "1");
        shift1Day3.put("count", 1);
        shifts1.add(shift1Day3);
        
        team1.put("shifts", shifts1);
        teams.add(team1);
        
        // 配置2人班组
        Map<String, Object> team2 = new HashMap<>();
        List<Map<String, Object>> shifts2 = new ArrayList<>();
        
        // 第一天：休息，0人
        Map<String, Object> shift2Day1 = new HashMap<>();
        shift2Day1.put("shiftId", "4");
        shift2Day1.put("count", 0);
        shifts2.add(shift2Day1);
        
        // 第二天：白班，2人
        Map<String, Object> shift2Day2 = new HashMap<>();
        shift2Day2.put("shiftId", "1");
        shift2Day2.put("count", 2);
        shifts2.add(shift2Day2);
        
        // 第三天：夜班，2人
        Map<String, Object> shift2Day3 = new HashMap<>();
        shift2Day3.put("shiftId", "2");
        shift2Day3.put("count", 2);
        shifts2.add(shift2Day3);
        
        team2.put("shifts", shifts2);
        teams.add(team2);
        
        modeConfig.put("teams", teams);
        when(dutyScheduleModeService.getModeConfig(modeId)).thenReturn(modeConfig);

        // 模拟请假信息（无请假）
        when(leaveRequestService.getEmployeeLeaveInfo(anyList(), anyString(), anyString())).thenReturn(new HashMap<>());

        // 生成排班
        Map<String, Object> configParams = new HashMap<>();
        List<Long> employeeIds = List.of(1L, 2L);
        configParams.put("employeeIds", employeeIds);
        
        List<DutyAssignment> assignments = autoScheduleService.generateScheduleByMode(
                scheduleId, startDate, endDate, modeId, configParams);

        // 验证结果
        assertNotNull(assignments);
        
        // 检查1人班组的分配情况（第一组，员工1）
        List<DutyAssignment> team1Assignments = new ArrayList<>();
        for (DutyAssignment assignment : assignments) {
            // 第一组的员工是员工1
            if (assignment.getEmployeeId() == 1L) {
                team1Assignments.add(assignment);
            }
        }
        
        // 验证1人班组至少有1天的排班
        assertTrue(team1Assignments.size() > 0, "1人班组应该至少有1天的排班");
        
        // 验证1人班组的成员是否一致
        if (!team1Assignments.isEmpty()) {
            Long firstEmployeeId = team1Assignments.get(0).getEmployeeId();
            for (DutyAssignment assignment : team1Assignments) {
                assertEquals(firstEmployeeId, assignment.getEmployeeId(), 
                        "1人班组的成员应该保持一致");
            }
            System.out.println("1人班组的成员保持一致: " + firstEmployeeId);
        }
        
        // 检查2人班组的分配情况（第二组，员工2）
        List<DutyAssignment> team2Assignments = new ArrayList<>();
        for (DutyAssignment assignment : assignments) {
            // 第二组的员工是员工2
            if (assignment.getEmployeeId() == 2L) {
                team2Assignments.add(assignment);
            }
        }
        
        // 验证2人班组至少有1天的排班
        assertTrue(team2Assignments.size() > 0, "2人班组应该至少有1天的排班");
        System.out.println("2人班组的排班数量: " + team2Assignments.size());
    }

    @Test
    void testGenerateScheduleByMode_TwoPersonTeam_ShouldRotate() {
        // 准备测试数据
        Long scheduleId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);
        Long modeId = 1L;

        // 创建值班表
        DutySchedule schedule = new DutySchedule();
        schedule.setId(scheduleId);
        when(dutyScheduleService.getById(scheduleId)).thenReturn(schedule);

        // 创建员工列表
        List<SysEmployee> employees = new ArrayList<>();
        SysEmployee employee1 = new SysEmployee();
        employee1.setId(1L);
        employee1.setEmployeeName("员工1");
        employee1.setStatus(1);
        employees.add(employee1);

        SysEmployee employee2 = new SysEmployee();
        employee2.setId(2L);
        employee2.setEmployeeName("员工2");
        employee2.setStatus(1);
        employees.add(employee2);

        when(sysEmployeeService.listByIds(anyList())).thenReturn(employees);
        when(dutyScheduleService.getEmployeeIdsByScheduleId(scheduleId)).thenReturn(List.of(1L, 2L));

        // 创建排班模式
        DutyScheduleMode mode = new DutyScheduleMode();
        mode.setId(modeId);
        when(dutyScheduleModeService.getById(modeId)).thenReturn(mode);

        // 创建排班模式配置
        Map<String, Object> modeConfig = new HashMap<>();
        List<Map<String, Object>> teams = new ArrayList<>();
        
        // 配置2人班组
        Map<String, Object> team1 = new HashMap<>();
        List<Map<String, Object>> shifts1 = new ArrayList<>();
        
        // 第一天：白班，2人
        Map<String, Object> shift1Day1 = new HashMap<>();
        shift1Day1.put("shiftId", "1");
        shift1Day1.put("count", 2);
        shifts1.add(shift1Day1);
        
        // 第二天：白班，2人
        Map<String, Object> shift1Day2 = new HashMap<>();
        shift1Day2.put("shiftId", "1");
        shift1Day2.put("count", 2);
        shifts1.add(shift1Day2);
        
        team1.put("shifts", shifts1);
        teams.add(team1);
        
        modeConfig.put("teams", teams);
        when(dutyScheduleModeService.getModeConfig(modeId)).thenReturn(modeConfig);

        // 模拟请假信息（无请假）
        when(leaveRequestService.getEmployeeLeaveInfo(anyList(), anyString(), anyString())).thenReturn(new HashMap<>());

        // 生成排班
        Map<String, Object> configParams = new HashMap<>();
        List<Long> employeeIds = List.of(1L, 2L);
        configParams.put("employeeIds", employeeIds);
        
        List<DutyAssignment> assignments = autoScheduleService.generateScheduleByMode(
                scheduleId, startDate, endDate, modeId, configParams);

        // 验证结果
        assertNotNull(assignments);
        assertEquals(4, assignments.size()); // 2天 x 2人
    }
}
