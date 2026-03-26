package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.PmProject;
import com.lasu.hyperduty.entity.PmTask;
import com.lasu.hyperduty.mapper.PmProjectMapper;
import com.lasu.hyperduty.mapper.PmTaskMapper;
import com.lasu.hyperduty.service.PmTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmTaskServiceImpl extends ServiceImpl<PmTaskMapper, PmTask> implements PmTaskService {

    private final PmProjectMapper projectMapper;

    @Override
    public Page<PmTask> pageList(Integer pageNum, Integer pageSize, Long projectId, Long assigneeId, Integer status, Integer priority) {
        Page<PmTask> page = new Page<>(pageNum, pageSize);
        
        // 查询总记录数
        Long total = baseMapper.selectTaskCount(projectId, assigneeId, status, priority);
        page.setTotal(total);
        
        // 查询带关联的数据
        List<PmTask> records = baseMapper.selectTaskPage(projectId, assigneeId, status, priority);
        
        // 手动分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, records.size());
        if (fromIndex < records.size()) {
            page.setRecords(records.subList(fromIndex, toIndex));
        } else {
            page.setRecords(new java.util.ArrayList<>());
        }
        
        return page;
    }

    @Override
    public List<PmTask> getProjectTasks(Long projectId) {
        return baseMapper.selectByProjectId(projectId);
    }

    @Override
    public List<PmTask> getSubTasks(Long parentId) {
        return baseMapper.selectByParentId(parentId);
    }

    @Override
    public List<PmTask> getMyTasks(Long employeeId) {
        return baseMapper.selectMyTasks(employeeId);
    }

    @Override
    public List<PmTask> getMyTasksByProject(Long employeeId, Long projectId) {
        return baseMapper.selectMyTasksByProject(employeeId, projectId);
    }

    @Override
    public PmTask getTaskDetail(Long id) {
        return baseMapper.selectTaskById(id);
    }

    @Override
    @Transactional
    public PmTask createTask(PmTask task) {
        if (task.getParentId() == null || task.getParentId() == 0) {
            task.setParentId(0L);
            task.setTaskLevel(1);
        } else {
            PmTask parentTask = getById(task.getParentId());
            if (parentTask != null) {
                task.setTaskLevel(parentTask.getTaskLevel() + 1);
                if (task.getTaskLevel() > 3) {
                    throw new RuntimeException("任务层级不能超过3级");
                }
            }
        }
        
        // 生成任务编码
        if (task.getTaskCode() == null || task.getTaskCode().isEmpty()) {
            PmProject project = projectMapper.selectById(task.getProjectId());
            String projectCode = project != null && project.getProjectCode() != null ? project.getProjectCode() : "TASK";
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            task.setTaskCode(projectCode + "-" + timestamp);
        }
        
        // 如果前端没有传 createBy，从 SecurityContext 中获取当前用户
        if (task.getCreateBy() == null) {
            try {
                org.springframework.security.core.Authentication authentication = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() != null) {
                    String principal = authentication.getPrincipal().toString();
                    // 尝试从 principal 中获取 employeeId
                    // 这里简化处理，实际项目中可能需要更复杂的逻辑
                    if (principal.contains("employeeId")) {
                        // 如果 principal 是自定义对象，可以强转为对应的类型
                        // 这里暂时不做处理，让前端传 createBy
                    }
                }
            } catch (Exception e) {
                log.warn("获取当前用户信息失败", e);
            }
        }
        
        task.setStatus(1);
        task.setProgress(0);
        task.setIsPinned(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        save(task);
        
        updateProjectProgress(task.getProjectId());
        
        log.info("创建任务成功: {}", task.getTaskName());
        return task;
    }

    @Override
    @Transactional
    public PmTask updateTask(PmTask task) {
        task.setUpdateTime(LocalDateTime.now());
        updateById(task);
        
        updateProjectProgress(task.getProjectId());
        
        log.info("更新任务成功: {}", task.getId());
        return task;
    }

    @Override
    @Transactional
    public void updateProgress(Long taskId, Integer progress) {
        PmTask task = getById(taskId);
        if (task != null) {
            task.setProgress(progress);
            task.setUpdateTime(LocalDateTime.now());
            
            if (progress >= 100) {
                task.setStatus(3);
            } else if (progress > 0) {
                task.setStatus(2);
            }
            
            updateById(task);
            updateProjectProgress(task.getProjectId());
            log.info("更新任务进度成功: taskId={}, progress={}", taskId, progress);
        }
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        PmTask task = getById(id);
        if (task != null) {
            Long projectId = task.getProjectId();
            
            deleteSubTasks(id);
            
            removeById(id);
            
            updateProjectProgress(projectId);
            log.info("删除任务成功: {}", id);
        }
    }

    private void deleteSubTasks(Long parentId) {
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmTask::getParentId, parentId);
        List<PmTask> subTasks = list(wrapper);
        
        for (PmTask subTask : subTasks) {
            deleteSubTasks(subTask.getId());
            removeById(subTask.getId());
        }
    }

    @Override
    public List<PmTask> getUpcomingTasks() {
        return baseMapper.selectUpcomingTasks();
    }

    @Override
    public List<PmTask> getTasksByStatus(Integer status) {
        return baseMapper.selectByStatus(status);
    }

    @Override
    @Transactional
    public void pinTask(Long taskId, boolean pinned) {
        PmTask task = getById(taskId);
        if (task != null) {
            task.setIsPinned(pinned ? 1 : 0);
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);
            log.info("置顶任务成功: taskId={}, pinned={}", taskId, pinned);
        }
    }

    @Override
    public Integer calculateProjectProgress(Long projectId) {
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmTask::getProjectId, projectId)
               .eq(PmTask::getTaskLevel, 1);
        
        List<PmTask> tasks = list(wrapper);
        
        if (tasks.isEmpty()) {
            return 0;
        }
        
        int totalProgress = 0;
        for (PmTask task : tasks) {
            totalProgress += calculateTaskProgress(task);
        }
        
        return totalProgress / tasks.size();
    }

    private Integer calculateTaskProgress(PmTask task) {
        if (task.getProgress() != null && task.getProgress() >= 100) {
            return 100;
        }
        
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmTask::getParentId, task.getId());
        List<PmTask> subTasks = list(wrapper);
        
        if (subTasks.isEmpty()) {
            return task.getProgress() != null ? task.getProgress() : 0;
        }
        
        int totalProgress = 0;
        for (PmTask subTask : subTasks) {
            totalProgress += calculateTaskProgress(subTask);
        }
        
        return totalProgress / subTasks.size();
    }

    private void updateProjectProgress(Long projectId) {
        Integer progress = calculateProjectProgress(projectId);
        
        PmProject project = projectMapper.selectById(projectId);
        if (project != null) {
            project.setProgress(progress);
            project.setUpdateTime(LocalDateTime.now());
            projectMapper.updateById(project);
        }
    }

    @Override
    public boolean hasTaskPermission(Long taskId, Long employeeId) {
        // 如果employeeId为null，直接返回false
        if (employeeId == null) {
            return false;
        }
        // 获取任务信息
        PmTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        
        // 检查是否是任务的创建者
        if (task.getCreateBy() != null && task.getCreateBy().equals(employeeId)) {
            return true;
        }
        
        // 检查是否是任务的负责人
        if (task.getAssigneeId() != null && task.getAssigneeId().equals(employeeId)) {
            return true;
        }
        
        // 检查是否是项目的所有者
        PmProject project = projectMapper.selectById(task.getProjectId());
        if (project != null && project.getOwnerId() != null && project.getOwnerId().equals(employeeId)) {
            return true;
        }
        
        // 检查是否是项目的参与人员
        if (project != null && project.getParticipants() != null) {
            List<Long> participants = project.getParticipants();
            if (participants.contains(employeeId)) {
                return true;
            }
        }
        
        // 检查是否是任务的干系人
        if (task.getStakeholders() != null) {
            String stakeholders = task.getStakeholders();
            try {
                // 解析JSON字符串为数组
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<Long> stakeholderList = mapper.readValue(stakeholders, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Long>>() {});
                if (stakeholderList.contains(employeeId)) {
                    return true;
                }
            } catch (Exception e) {
                // 如果解析失败，尝试直接检查字符串
                if (stakeholders.contains("[" + employeeId + "]") || 
                    stakeholders.contains("," + employeeId + ",") || 
                    stakeholders.startsWith(employeeId + ",") || 
                    stakeholders.endsWith("," + employeeId)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean hasTaskDeletePermission(Long taskId, Long employeeId) {
        // 如果employeeId为null，直接返回false
        if (employeeId == null) {
            return false;
        }
        // 获取任务信息
        PmTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        
        // 检查是否是任务的创建者
        if (task.getCreateBy() != null && task.getCreateBy().equals(employeeId)) {
            return true;
        }
        
        // 检查是否是任务的负责人
        if (task.getAssigneeId() != null && task.getAssigneeId().equals(employeeId)) {
            return true;
        }
        
        // 检查是否是项目的所有者
        PmProject project = projectMapper.selectById(task.getProjectId());
        if (project != null && project.getOwnerId() != null && project.getOwnerId().equals(employeeId)) {
            return true;
        }
        
        // 检查是否是项目的参与人员
        if (project != null && project.getParticipants() != null) {
            List<Long> participants = project.getParticipants();
            if (participants.contains(employeeId)) {
                return true;
            }
        }
        
        // 任务干系人没有删除权限
        return false;
    }
}
