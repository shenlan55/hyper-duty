package com.lasu.hyperduty.workflow.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lasu.hyperduty.workflow.entity.WfDelegate;
import com.lasu.hyperduty.workflow.mapper.WfDelegateMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 委托自动转办定时任务
 */
@Slf4j
@Component
public class DelegateAutoTransferTask {

    @Autowired
    private WfDelegateMapper delegateMapper;

    @Autowired
    private TaskService taskService;

    /**
     * 定时执行委托转办
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void executeDelegateTransfer() {
        log.info("========== 开始执行委托自动转办任务 ==========");
        
        try {
            // 1. 查询所有启用且在有效期内的委托配置
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<WfDelegate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WfDelegate::getStatus, 1)
                    .le(WfDelegate::getStartTime, now)
                    .ge(WfDelegate::getEndTime, now);
            
            List<WfDelegate> delegateList = delegateMapper.selectList(wrapper);
            
            if (delegateList == null || delegateList.isEmpty()) {
                log.info("无有效的委托配置，跳过");
                return;
            }
            
            log.info("找到 {} 条有效的委托配置", delegateList.size());
            
            // 2. 遍历每个委托配置，处理待办任务
            for (WfDelegate delegate : delegateList) {
                try {
                    processDelegate(delegate);
                } catch (Exception e) {
                    log.error("处理委托配置失败: delegateId={}", delegate.getId(), e);
                }
            }
            
            log.info("========== 委托自动转办任务执行完成 ==========");
            
        } catch (Exception e) {
            log.error("执行委托自动转办任务失败", e);
        }
    }

    /**
     * 处理单个委托配置
     */
    private void processDelegate(WfDelegate delegate) {
        Long sourceUserId = delegate.getDelegatorId();
        Long targetUserId = delegate.getAttorneyId();
        String reason = delegate.getRemark();
        
        log.info("处理委托配置: {} -> {}, reason={}", 
                sourceUserId, targetUserId, reason);
        
        // 查询 sourceUserId 的待办任务
        List<Task> taskList = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(sourceUserId))
                .list();
        
        if (taskList == null || taskList.isEmpty()) {
            log.debug("用户 {} 无待办任务", sourceUserId);
            return;
        }
        
        log.info("用户 {} 有 {} 个待办任务需要转办", 
                sourceUserId, taskList.size());
        
        // 遍历任务，逐个转办
        for (Task task : taskList) {
            try {
                // 转办任务
                taskService.setAssignee(task.getId(), String.valueOf(targetUserId));
                
                // 添加任务意见
                taskService.addComment(task.getId(), 
                        task.getProcessInstanceId(), 
                        "委托转办：" + reason);
                
                log.info("任务转办成功: taskId={}, taskName={}, {} -> {}", 
                        task.getId(), task.getName(), sourceUserId, targetUserId);
                
            } catch (Exception e) {
                log.error("任务转办失败: taskId={}", task.getId(), e);
            }
        }
    }
}
