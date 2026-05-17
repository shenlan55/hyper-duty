package com.lasu.hyperduty.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskUpdateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskVO;
import com.lasu.hyperduty.pm.entity.PmShadowAnnotation;
import com.lasu.hyperduty.pm.entity.PmTask;
import com.lasu.hyperduty.pm.entity.PmTaskShadow;
import com.lasu.hyperduty.pm.mapper.PmShadowAnnotationMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskShadowMapper;
import com.lasu.hyperduty.pm.service.PmTaskShadowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 影子任务 Service 实现 (v2)
 */
@Service
@RequiredArgsConstructor
public class PmTaskShadowServiceImpl implements PmTaskShadowService {

    private final PmTaskShadowMapper taskShadowMapper;
    private final PmTaskMapper taskMapper;
    private final PmShadowAnnotationMapper annotationMapper;

    // ========================================
    // 影子任务 CRUD
    // ========================================

    @Override
    @Transactional
    public PmTaskShadow createShadow(ShadowTaskCreateDTO dto, String username) {
        // 1. 验证源任务存在
        PmTask sourceTask = taskMapper.selectById(dto.getSourceTaskId());
        if (sourceTask == null) {
            throw new RuntimeException("源任务不存在");
        }

        // 2. 检查唯一性（源任务+项目必须唯一）
        LambdaQueryWrapper<PmTaskShadow> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(PmTaskShadow::getSourceTaskId, dto.getSourceTaskId())
                .eq(PmTaskShadow::getProjectId, dto.getProjectId());
        if (taskShadowMapper.selectCount(checkWrapper) > 0) {
            throw new RuntimeException("该任务已在此项目中创建过影子");
        }

        // 3. 创建影子
        PmTaskShadow shadow = new PmTaskShadow();
        shadow.setSourceTaskId(dto.getSourceTaskId());
        shadow.setProjectId(dto.getProjectId());
        shadow.setShadowAlias(dto.getShadowAlias());
        shadow.setShadowDescription(dto.getShadowDescription());
        shadow.setCreatedBy(username);
        shadow.setCreatedAt(LocalDateTime.now());
        shadow.setUpdatedAt(LocalDateTime.now());
        taskShadowMapper.insert(shadow);

        return shadow;
    }

    @Override
    @Transactional
    public PmTaskShadow updateShadow(Long shadowId, ShadowTaskUpdateDTO dto) {
        PmTaskShadow shadow = taskShadowMapper.selectById(shadowId);
        if (shadow == null) {
            throw new RuntimeException("影子任务不存在");
        }

        if (dto.getShadowAlias() != null) {
            shadow.setShadowAlias(dto.getShadowAlias());
        }
        if (dto.getShadowDescription() != null) {
            shadow.setShadowDescription(dto.getShadowDescription());
        }
        shadow.setUpdatedAt(LocalDateTime.now());
        taskShadowMapper.updateById(shadow);

        return shadow;
    }

    @Override
    @Transactional
    public void deleteShadow(Long shadowId) {
        // 由于数据库设置了 ON DELETE CASCADE，删除影子会自动删除批注
        taskShadowMapper.deleteById(shadowId);
    }

    // ========================================
    // 查询
    // ========================================

    @Override
    public List<ShadowTaskVO> getTaskListWithShadows(Long projectId, Long currentEmployeeId) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        List<ShadowTaskVO> tasks = taskShadowMapper.selectTaskListWithShadows(projectId);
        for (ShadowTaskVO task : tasks) {
            if (task.getIsShadow() != null && task.getIsShadow() == 1) {
                // 影子任务
                PmTaskShadow shadow = taskShadowMapper.selectById(task.getId());
                if (shadow != null) {
                    boolean isOwner = shadow.getCreatedBy() != null && currentUsername != null && shadow.getCreatedBy().equals(currentUsername);
                    task.setHasPermission(isOwner);
                    task.setHasDeletePermission(isOwner);
                }
            } else {
                // 真实任务，先设置权限为true，让按钮显示
                task.setHasPermission(true);
                task.setHasDeletePermission(true);
            }
        }
        return tasks;
    }

    @Override
    public ShadowTaskVO getShadowDetail(Long shadowId, Long currentEmployeeId) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        ShadowTaskVO task = taskShadowMapper.selectShadowById(shadowId);
        if (task != null) {
            PmTaskShadow shadow = taskShadowMapper.selectById(shadowId);
            if (shadow != null) {
                boolean isOwner = shadow.getCreatedBy() != null && currentUsername != null && shadow.getCreatedBy().equals(currentUsername);
                task.setHasPermission(isOwner);
                task.setHasDeletePermission(isOwner);
            }
        }
        return task;
    }

    @Override
    public List<PmTaskShadow> getShadowsBySourceTask(Long sourceTaskId) {
        return taskShadowMapper.selectShadowsBySourceTaskId(sourceTaskId);
    }

    // ========================================
    // 批注 CRUD
    // ========================================

    @Override
    @Transactional
    public PmShadowAnnotation addAnnotation(ShadowAnnotationCreateDTO dto, String username) {
        // 验证影子存在
        PmTaskShadow shadow = taskShadowMapper.selectById(dto.getShadowId());
        if (shadow == null) {
            throw new RuntimeException("影子任务不存在");
        }

        PmShadowAnnotation annotation = new PmShadowAnnotation();
        annotation.setShadowId(dto.getShadowId());
        annotation.setContent(dto.getContent());
        annotation.setCreatedBy(username);
        annotation.setCreatedAt(LocalDateTime.now());
        annotation.setUpdatedAt(LocalDateTime.now());
        annotationMapper.insert(annotation);

        return annotation;
    }

    @Override
    @Transactional
    public void deleteAnnotation(Long annotationId) {
        annotationMapper.deleteById(annotationId);
    }

    @Override
    public List<PmShadowAnnotation> getAnnotationsByShadowId(Long shadowId) {
        LambdaQueryWrapper<PmShadowAnnotation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmShadowAnnotation::getShadowId, shadowId)
                .orderByDesc(PmShadowAnnotation::getCreatedAt);
        return annotationMapper.selectList(wrapper);
    }
}
