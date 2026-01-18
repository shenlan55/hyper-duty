package com.lasu.hyperduty.service;

import com.lasu.hyperduty.entity.ApprovalRecord;

import java.util.List;

public interface ApprovalRecordService extends com.baomidou.mybatisplus.extension.service.IService<ApprovalRecord> {

    void addApprovalRecord(Long requestId, String requestType, Long approverId, 
                         Integer approvalLevel, String approvalStatus, String approvalOpinion);

    List<ApprovalRecord> getApprovalRecordsByRequestId(Long requestId);

    boolean canApprove(Long requestId, Long approverId);
}
