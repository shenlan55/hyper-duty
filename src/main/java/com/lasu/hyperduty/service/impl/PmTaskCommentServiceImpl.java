package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.PmTaskComment;
import com.lasu.hyperduty.mapper.PmTaskCommentMapper;
import com.lasu.hyperduty.service.PmTaskCommentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务批注服务实现类
 */
@Service
public class PmTaskCommentServiceImpl extends ServiceImpl<PmTaskCommentMapper, PmTaskComment> implements PmTaskCommentService {

    @Override
    public List<PmTaskComment> getCommentsByTaskId(Long taskId) {
        LambdaQueryWrapper<PmTaskComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PmTaskComment::getTaskId, taskId);
        queryWrapper.orderByDesc(PmTaskComment::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean addComment(PmTaskComment comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        return save(comment);
    }

}
