package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.workflow.dto.WfUrgeDTO;
import com.lasu.hyperduty.workflow.entity.WfUrgeRecord;
import com.lasu.hyperduty.workflow.mapper.WfUrgeRecordMapper;
import com.lasu.hyperduty.workflow.service.WfUrgeService;
import lombok.RequiredArgsConstructor;
import org.flowable.task.api.Task;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class WfUrgeServiceImpl implements WfUrgeService {

    private final WfUrgeRecordMapper urgeRecordMapper;
    private final TaskService taskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urge(WfUrgeDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTaskId())) {
            throw new BusinessException("任务ID不能为空");
        }
        if (dto.getToUserId() == null) {
            throw new BusinessException("催办接收人不能为空");
        }
        if (!StringUtils.hasText(dto.getContent()) || dto.getContent().trim().isEmpty()) {
            throw new BusinessException("催办内容不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已结束：" + dto.getTaskId());
        }
        WfUrgeRecord record = new WfUrgeRecord();
        record.setProcessInstanceId(task.getProcessInstanceId());
        record.setTaskId(dto.getTaskId());
        record.setFromUserId(SecurityUtil.getCurrentUserId());
        record.setToUserId(dto.getToUserId());
        record.setContent(dto.getContent().trim());
        record.setCreateTime(new Date());
        urgeRecordMapper.insert(record);
        // 记录到 task comment
        taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(),
                "【催办】 from=" + record.getFromUserId() + " to=" + dto.getToUserId() + " " + record.getContent());
    }

    @Override
    public IPage<WfUrgeRecord> pageSent(IPage<WfUrgeRecord> page) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return urgeRecordMapper.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfUrgeRecord>()
                        .eq(WfUrgeRecord::getFromUserId, currentUserId)
                        .orderByDesc(WfUrgeRecord::getCreateTime));
    }

    @Override
    public IPage<WfUrgeRecord> pageReceived(IPage<WfUrgeRecord> page) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return urgeRecordMapper.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfUrgeRecord>()
                        .eq(WfUrgeRecord::getToUserId, currentUserId)
                        .orderByDesc(WfUrgeRecord::getCreateTime));
    }
}
