package com.lasu.hyperduty.duty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.duty.entity.ApprovalRecord;
import com.lasu.hyperduty.duty.entity.LeaveRequest;
import com.lasu.hyperduty.duty.mapper.ApprovalRecordMapper;
import com.lasu.hyperduty.duty.service.ApprovalRecordService;
import com.lasu.hyperduty.duty.service.LeaveRequestService;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.entity.SysRole;
import com.lasu.hyperduty.system.entity.SysUserRole;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import com.lasu.hyperduty.system.service.SysRoleService;
import com.lasu.hyperduty.system.service.SysUserRoleService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;











@Service
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> implements ApprovalRecordService {

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public void addApprovalRecord(Long requestId, String requestType, Long approverId, 
                               Integer approvalLevel, String approvalStatus, String approvalOpinion) {
        ApprovalRecord record = new ApprovalRecord();
        record.setRequestId(requestId);
        record.setRequestType(requestType);
        record.setApproverId(approverId);
        record.setApprovalLevel(approvalLevel);
        record.setApprovalStatus(approvalStatus);
        record.setApprovalOpinion(approvalOpinion);
        record.setApprovalTime(LocalDateTime.now());
        save(record);
    }

    @Override
    public List<ApprovalRecord> getApprovalRecordsByRequestId(Long requestId) {
        return lambdaQuery()
                .eq(ApprovalRecord::getRequestId, requestId)
                .orderByAsc(ApprovalRecord::getApprovalLevel)
                .list();
    }

    @Override
    public boolean canApprove(Long requestId, Long approverId) {
        LeaveRequest request = leaveRequestService.getById(requestId);
        if (request == null) {
            return false;
        }

        SysEmployee employee = sysEmployeeService.getById(approverId);
        if (employee == null) {
            return false;
        }

        List<SysUserRole> userRoles = sysUserRoleService.lambdaQuery()
                .eq(SysUserRole::getEmployeeId, approverId)
                .list();

        for (SysUserRole userRole : userRoles) {
            SysRole role = sysRoleService.getById(userRole.getRoleId());
            if (role != null && "ROLE_ADMIN".equals(role.getRoleCode())) {
                return true;
            }
        }

        Integer currentLevel = request.getApprovalLevel();
        Integer totalLevels = request.getTotalApprovalLevels();

        if (currentLevel != null && totalLevels != null) {
            if (currentLevel > totalLevels) {
                return false;
            }
        }

        return true;
    }
}
