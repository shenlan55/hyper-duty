package com.lasu.hyperduty.service;

import com.lasu.hyperduty.entity.SwapRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 调班申请服务测试类
 */
public class SwapRequestServiceTest {

    private SwapRequest mockSwapRequest;

    @BeforeEach
    public void setUp() {
        // 创建模拟调班申请数据
        mockSwapRequest = new SwapRequest();
        mockSwapRequest.setId(1L);
        mockSwapRequest.setRequestNo("SW202401010001");
        mockSwapRequest.setOriginalEmployeeId(1L);
        mockSwapRequest.setTargetEmployeeId(2L);
        mockSwapRequest.setScheduleId(1L);
        mockSwapRequest.setSwapDate(LocalDateTime.now().toLocalDate());
        mockSwapRequest.setSwapShift(1);
        mockSwapRequest.setReason("个人原因");
        mockSwapRequest.setApprovalStatus("pending");
        mockSwapRequest.setOriginalConfirmStatus("pending");
        mockSwapRequest.setTargetConfirmStatus("pending");
        mockSwapRequest.setCreateTime(LocalDateTime.now());
        mockSwapRequest.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 测试调班申请基本属性
     */
    @Test
    public void testSwapRequestBasicProperties() {
        assertNotNull(mockSwapRequest);
        assertEquals(1L, mockSwapRequest.getId());
        assertEquals("SW202401010001", mockSwapRequest.getRequestNo());
        assertEquals(1L, mockSwapRequest.getOriginalEmployeeId());
        assertEquals(2L, mockSwapRequest.getTargetEmployeeId());
        assertEquals(1L, mockSwapRequest.getScheduleId());
        assertEquals(1, mockSwapRequest.getSwapShift());
        assertEquals("个人原因", mockSwapRequest.getReason());
        assertEquals("pending", mockSwapRequest.getApprovalStatus());
        assertEquals("pending", mockSwapRequest.getOriginalConfirmStatus());
        assertEquals("pending", mockSwapRequest.getTargetConfirmStatus());
        assertNotNull(mockSwapRequest.getCreateTime());
        assertNotNull(mockSwapRequest.getUpdateTime());
    }

    /**
     * 测试调班申请状态转换逻辑
     */
    @Test
    public void testSwapRequestStatusTransition() {
        // 测试初始状态
        assertEquals("pending", mockSwapRequest.getApprovalStatus());
        assertEquals("pending", mockSwapRequest.getOriginalConfirmStatus());
        assertEquals("pending", mockSwapRequest.getTargetConfirmStatus());

        // 测试原值班人员确认
        mockSwapRequest.setOriginalConfirmStatus("confirmed");
        assertEquals("confirmed", mockSwapRequest.getOriginalConfirmStatus());
        assertEquals("pending", mockSwapRequest.getTargetConfirmStatus());

        // 测试目标值班人员确认
        mockSwapRequest.setTargetConfirmStatus("confirmed");
        assertEquals("confirmed", mockSwapRequest.getOriginalConfirmStatus());
        assertEquals("confirmed", mockSwapRequest.getTargetConfirmStatus());

        // 测试审批通过
        mockSwapRequest.setApprovalStatus("approved");
        assertEquals("approved", mockSwapRequest.getApprovalStatus());

        // 测试审批拒绝
        mockSwapRequest.setApprovalStatus("rejected");
        assertEquals("rejected", mockSwapRequest.getApprovalStatus());
    }

    /**
     * 测试调班申请边界情况
     */
    @Test
    public void testSwapRequestEdgeCases() {
        // 测试空值情况
        SwapRequest emptyRequest = new SwapRequest();
        assertNotNull(emptyRequest);
        assertNull(emptyRequest.getId());
        assertNull(emptyRequest.getRequestNo());
        assertNull(emptyRequest.getOriginalEmployeeId());
        assertNull(emptyRequest.getTargetEmployeeId());
        assertNull(emptyRequest.getScheduleId());
        assertNull(emptyRequest.getSwapDate());
        assertNull(emptyRequest.getSwapShift());
        assertNull(emptyRequest.getReason());
        assertNull(emptyRequest.getApprovalStatus());
        assertNull(emptyRequest.getOriginalConfirmStatus());
        assertNull(emptyRequest.getTargetConfirmStatus());
        assertNull(emptyRequest.getCreateTime());
        assertNull(emptyRequest.getUpdateTime());
    }

    /**
     * 测试调班申请数据完整性
     */
    @Test
    public void testSwapRequestDataIntegrity() {
        // 测试调班申请数据一致性
        assertEquals(mockSwapRequest.getId(), 1L);
        assertEquals(mockSwapRequest.getOriginalEmployeeId(), 1L);
        assertEquals(mockSwapRequest.getTargetEmployeeId(), 2L);
        assertEquals(mockSwapRequest.getScheduleId(), 1L);
        assertEquals(mockSwapRequest.getSwapShift(), 1);
        assertEquals(mockSwapRequest.getReason(), "个人原因");

        // 测试时间戳有效性
        assertNotNull(mockSwapRequest.getCreateTime());
        assertNotNull(mockSwapRequest.getUpdateTime());
        assertTrue(mockSwapRequest.getCreateTime().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(mockSwapRequest.getUpdateTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    /**
     * 测试调班申请状态组合
     */
    @Test
    public void testSwapRequestStatusCombinations() {
        // 测试各种状态组合
        String[] approvalStatuses = {"pending", "approved", "rejected", "cancelled"};
        String[] confirmStatuses = {"pending", "confirmed", "rejected"};

        for (String approvalStatus : approvalStatuses) {
            for (String originalConfirmStatus : confirmStatuses) {
                for (String targetConfirmStatus : confirmStatuses) {
                    SwapRequest request = new SwapRequest();
                    request.setApprovalStatus(approvalStatus);
                    request.setOriginalConfirmStatus(originalConfirmStatus);
                    request.setTargetConfirmStatus(targetConfirmStatus);

                    assertEquals(approvalStatus, request.getApprovalStatus());
                    assertEquals(originalConfirmStatus, request.getOriginalConfirmStatus());
                    assertEquals(targetConfirmStatus, request.getTargetConfirmStatus());
                }
            }
        }
    }
}


