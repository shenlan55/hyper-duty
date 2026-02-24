package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.PmTaskComment;

import java.util.List;

/**
 * 任务批注服务接口
 */
public interface PmTaskCommentService extends IService<PmTaskComment> {

    /**
     * 根据任务ID获取批注列表
     * @param taskId 任务ID
     * @return 批注列表
     */
    List<PmTaskComment> getCommentsByTaskId(Long taskId);

    /**
     * 添加任务批注
     * @param comment 批注信息
     * @return 是否添加成功
     */
    boolean addComment(PmTaskComment comment);

}
