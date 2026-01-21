package com.lasu.hyperduty.service.algorithm;

import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.algorithm.impl.FourDayRotationAlgorithm;
import com.lasu.hyperduty.service.algorithm.impl.ThreeDayRotationAlgorithm;
import com.lasu.hyperduty.service.algorithm.impl.DaytimeAllNightShiftRotationAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 排班算法测试类
 */
@ExtendWith(MockitoExtension.class)
public class ScheduleAlgorithmTest {

    private FourDayRotationAlgorithm fourDayRotationAlgorithm;
    private ThreeDayRotationAlgorithm threeDayRotationAlgorithm;
    private DaytimeAllNightShiftRotationAlgorithm daytimeAllNightShiftRotationAlgorithm;
    private DutySchedule mockSchedule;
    private List<SysEmployee> mockEmployees;

    @BeforeEach
    public void setUp() {
        // 初始化算法实例
        fourDayRotationAlgorithm = new FourDayRotationAlgorithm();
        threeDayRotationAlgorithm = new ThreeDayRotationAlgorithm();
        daytimeAllNightShiftRotationAlgorithm = new DaytimeAllNightShiftRotationAlgorithm();

        // 创建模拟数据
        mockSchedule = new DutySchedule();
        mockSchedule.setId(1L);
        mockSchedule.setScheduleName("测试值班表");

        // 创建4名模拟员工
        mockEmployees = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            SysEmployee employee = new SysEmployee();
            employee.setId((long) i);
            employee.setEmployeeName("员工" + i);
            employee.setEmployeeCode("EMP00" + i);
            mockEmployees.add(employee);
        }
    }

    /**
     * 测试四天一轮排班算法
     */
    @Test
    public void testFourDayRotationAlgorithm() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7); // 测试8天的排班
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("groupSize", 2);

        List<DutyAssignment> assignments = fourDayRotationAlgorithm.generateSchedule(
                mockSchedule, mockEmployees, startDate, endDate, configParams);

        // 验证排班结果
        assertNotNull(assignments);
        assertFalse(assignments.isEmpty());

        // 检查排班数量：8天 × 2组 × 1种班次（白班或夜班）
        // 四天一轮中每天都有一组在白班或夜班，所以8天中每天都有排班
        // 每组每天1个班次（白班或夜班），2组，所以总排班数应该是 8天 × 2组 × 1班次 = 16个排班
        assertEquals(16, assignments.size());

        // 验证每组每天的班次分配
        Map<LocalDate, Map<Integer, List<Long>>> dailyAssignments = new HashMap<>();
        assignments.forEach(assignment -> {
            dailyAssignments.computeIfAbsent(assignment.getDutyDate(), k -> new HashMap<>())
                    .computeIfAbsent(assignment.getDutyShift(), k -> new ArrayList<>())
                    .add(assignment.getEmployeeId());
        });

        // 检查排班分配
        // 统计有排班的天数
        int scheduledDays = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Map<Integer, List<Long>> dayAssignments = dailyAssignments.get(currentDate);
            if (dayAssignments != null) {
                scheduledDays++;
                // 班次应该是白班或夜班
                assertTrue(dayAssignments.containsKey(1) || dayAssignments.containsKey(3));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 8天中应该有6天有排班（两组轮班，每组4天中2天有排班）
        // 但实际上当两组都处于休息周期时，当天没有排班
        // 4天周期中可能有1天没有排班，8天中可能有2天没有排班
        assertTrue(scheduledDays >= 6);
        assertTrue(scheduledDays <= 8);
    }

    /**
     * 测试三天一轮排班算法
     */
    @Test
    public void testThreeDayRotationAlgorithm() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(5); // 测试6天的排班
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("groupSize", 2);

        List<DutyAssignment> assignments = threeDayRotationAlgorithm.generateSchedule(
                mockSchedule, mockEmployees, startDate, endDate, configParams);

        // 验证排班结果
        assertNotNull(assignments);
        assertFalse(assignments.isEmpty());

        // 检查排班数量：6天 × 2组 × 2种班次（白班和夜班）
        // 三天一轮中每组每3天排1天班，2组在6天中会排4天班
        // 每组每天2个班次（白班和夜班），所以总排班数应该是 4天 × 2组 × 2班次/组/天 = 16个排班
        assertEquals(16, assignments.size());

        // 验证每组每天的班次分配
        Map<LocalDate, Map<Integer, List<Long>>> dailyAssignments = new HashMap<>();
        assignments.forEach(assignment -> {
            dailyAssignments.computeIfAbsent(assignment.getDutyDate(), k -> new HashMap<>())
                    .computeIfAbsent(assignment.getDutyShift(), k -> new ArrayList<>())
                    .add(assignment.getEmployeeId());
        });

        // 检查排班分配
        // 统计有排班的天数
        int scheduledDays = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Map<Integer, List<Long>> dayAssignments = dailyAssignments.get(currentDate);
            if (dayAssignments != null) {
                scheduledDays++;
                // 有排班的天数应该有白班和夜班两种班次
                assertEquals(2, dayAssignments.size());
                assertTrue(dayAssignments.containsKey(1)); // 白班
                assertTrue(dayAssignments.containsKey(3)); // 夜班
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // 6天中应该有4天有排班（2组 × 2天/组）
        assertEquals(4, scheduledDays);
    }

    /**
     * 测试白班组内全员、夜班轮值算法
     */
    @Test
    public void testDaytimeAllNightShiftRotationAlgorithm() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(3); // 测试4天的排班
        Map<String, Object> configParams = new HashMap<>();
        configParams.put("nightShiftCount", 2);

        List<DutyAssignment> assignments = daytimeAllNightShiftRotationAlgorithm.generateSchedule(
                mockSchedule, mockEmployees, startDate, endDate, configParams);

        // 验证排班结果
        assertNotNull(assignments);
        assertFalse(assignments.isEmpty());

        // 检查排班数量：4天 × (4名员工白班 + 2名员工夜班) = 4 × 6 = 24个排班
        assertEquals(24, assignments.size());

        // 验证每天的班次分配
        Map<LocalDate, Map<Integer, List<Long>>> dailyAssignments = new HashMap<>();
        assignments.forEach(assignment -> {
            dailyAssignments.computeIfAbsent(assignment.getDutyDate(), k -> new HashMap<>())
                    .computeIfAbsent(assignment.getDutyShift(), k -> new ArrayList<>())
                    .add(assignment.getEmployeeId());
        });

        // 检查每天的班次分配
        LocalDate currentDate = startDate;
        int nightShiftIndex = 0;
        while (!currentDate.isAfter(endDate)) {
            Map<Integer, List<Long>> dayAssignments = dailyAssignments.get(currentDate);
            assertNotNull(dayAssignments);
            assertEquals(2, dayAssignments.size());

            // 白班应该有4名员工
            List<Long> dayShiftEmployees = dayAssignments.get(1);
            assertNotNull(dayShiftEmployees);
            assertEquals(4, dayShiftEmployees.size());

            // 夜班应该有2名员工
            List<Long> nightShiftEmployees = dayAssignments.get(3);
            assertNotNull(nightShiftEmployees);
            assertEquals(2, nightShiftEmployees.size());

            // 验证夜班员工是轮换的
            long expectedEmployee1 = mockEmployees.get(nightShiftIndex % 4).getId();
            long expectedEmployee2 = mockEmployees.get((nightShiftIndex + 1) % 4).getId();
            assertTrue(nightShiftEmployees.contains(expectedEmployee1));
            assertTrue(nightShiftEmployees.contains(expectedEmployee2));

            currentDate = currentDate.plusDays(1);
            nightShiftIndex += 2; // 每次夜班2人，所以索引加2
        }
    }

    /**
     * 测试算法参数验证
     */
    @Test
    public void testAlgorithmParams() {
        // 测试四天一轮算法参数
        List<AlgorithmParam> fourDayParams = fourDayRotationAlgorithm.getSupportedParams();
        assertNotNull(fourDayParams);
        assertEquals(1, fourDayParams.size());
        assertEquals("每组人数", fourDayParams.get(0).getParamName());
        assertEquals("groupSize", fourDayParams.get(0).getParamCode());

        // 测试三天一轮算法参数
        List<AlgorithmParam> threeDayParams = threeDayRotationAlgorithm.getSupportedParams();
        assertNotNull(threeDayParams);
        assertEquals(1, threeDayParams.size());
        assertEquals("每组人数", threeDayParams.get(0).getParamName());
        assertEquals("groupSize", threeDayParams.get(0).getParamCode());

        // 测试白班全员夜班轮值算法参数
        List<AlgorithmParam> daytimeAllParams = daytimeAllNightShiftRotationAlgorithm.getSupportedParams();
        assertNotNull(daytimeAllParams);
        assertEquals(1, daytimeAllParams.size());
        assertEquals("夜班值班人数", daytimeAllParams.get(0).getParamName());
        assertEquals("nightShiftCount", daytimeAllParams.get(0).getParamCode());
    }
}