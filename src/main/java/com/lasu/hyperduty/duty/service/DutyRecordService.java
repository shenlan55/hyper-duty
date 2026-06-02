package com.lasu.hyperduty.duty.service;

import com.lasu.hyperduty.common.dto.PageRequestDTO;
import com.lasu.hyperduty.common.dto.PageResponseDTO;
import com.lasu.hyperduty.common.service.BasePageService;
import com.lasu.hyperduty.duty.dto.DutyRecordDTO;
import com.lasu.hyperduty.duty.entity.DutyRecord;
import com.lasu.hyperduty.system.entity.SysEmployee;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;






/**
 * 加班记录服务
 * 处理加班记录相关的业务逻辑
 */
public interface DutyRecordService extends BasePageService<DutyRecord> {

    /**
     * 根据加班记录ID获取可用的替补人员列表
     * @param recordId 加班记录ID
     * @return 替补人员列表
     */
    List<SysEmployee> getAvailableSubstitutes(Long recordId);

    /**
     * 根据员工ID、加班日期和班次获取可用的替补人员列表
     * @param employeeId 员工ID
     * @param dutyDate 加班日期
     * @param dutyShift 班次
     * @return 替补人员列表
     */
    List<SysEmployee> getAvailableSubstitutes(Long employeeId, LocalDate dutyDate, Integer dutyShift);

    /**
     * 获取待审批的加班记录
     * @param employeeId 审批人ID
     * @return 待审批的加班记录列表
     */
    List<DutyRecordDTO> getPendingApprovals(Long employeeId);

    /**
     * 分页查询加班记录（返回DTO）
     * @param pageRequestDTO 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    PageResponseDTO<DutyRecordDTO> pageDTO(PageRequestDTO pageRequestDTO, Map<String, Object> params);

    /**
     * 将实体列表转换为DTO列表并填充关联信息
     * @param records 实体列表
     * @return DTO列表
     */
    List<DutyRecordDTO> fillDTOList(List<DutyRecord> records);
}