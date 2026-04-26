package com.lasu.hyperduty.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.pm.entity.PmTaskComment;
import com.lasu.hyperduty.pm.mapper.PmTaskCommentMapper;
import com.lasu.hyperduty.pm.service.PmTaskCommentService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;








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
