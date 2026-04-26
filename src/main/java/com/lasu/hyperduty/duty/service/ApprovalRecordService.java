package com.lasu.hyperduty.duty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.duty.entity.ApprovalRecord;
import com.lasu.hyperduty.duty.service.ApprovalRecordService;
import java.util.List;








public interface ApprovalRecordService extends IService<ApprovalRecord> {

    void addApprovalRecord(Long requestId, String requestType, Long approverId, 
                         Integer approvalLevel, String approvalStatus, String approvalOpinion);

    List<ApprovalRecord> getApprovalRecordsByRequestId(Long requestId);

    boolean canApprove(Long requestId, Long approverId);
}
