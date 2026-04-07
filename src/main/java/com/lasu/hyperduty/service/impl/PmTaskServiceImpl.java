package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.dto.WorkloadDTO;
import com.lasu.hyperduty.entity.*;
import com.lasu.hyperduty.mapper.*;
import com.lasu.hyperduty.service.PmProjectDeputyOwnerService;
import com.lasu.hyperduty.service.PmTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmTaskServiceImpl extends ServiceImpl<PmTaskMapper, PmTask> implements PmTaskService {

    private final PmProjectMapper projectMapper;
    private final PmProjectDeputyOwnerService pmProjectDeputyOwnerService;
    private final PmTaskCustomRowMapper taskCustomRowMapper;
    private final PmCustomTableMapper customTableMapper;
    private final PmCustomTableRowMapper customTableRowMapper;
    private final PmCustomTableColumnMapper customTableColumnMapper;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<PmTask> pageList(Integer pageNum, Integer pageSize, Long projectId, Long assigneeId, Integer status, Integer priority) {
        Page<PmTask> page = new Page<>(pageNum, pageSize);
        
        // 查询所有符合条件的任务
        List<PmTask> allTasks = baseMapper.selectTaskPage(projectId, assigneeId, status, priority);
        
        // 修正所有任务的层级
        fixTaskLevels(allTasks);
        
        // 构建完整的树形结构，得到根任务列表
        List<PmTask> rootTasks = buildFlatRootTasks(allTasks);
        
        // 只对根任务进行分页
        page.setTotal(rootTasks.size());
        
        // 手动对根任务分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, rootTasks.size());
        
        if (fromIndex < rootTasks.size()) {
            // 获取当前页的根任务
            List<PmTask> pageRootTasks = rootTasks.subList(fromIndex, toIndex);
            
            // 为每个根任务加载所有子任务，构建完整的树
            List<PmTask> result = new java.util.ArrayList<>();
            Map<Long, List<PmTask>> parentIdToChildrenMap = new java.util.HashMap<>();
            
            for (PmTask task : allTasks) {
                Long parentId = task.getParentId() != null ? task.getParentId() : 0L;
                if (!parentIdToChildrenMap.containsKey(parentId)) {
                    parentIdToChildrenMap.put(parentId, new java.util.ArrayList<>());
                }
                parentIdToChildrenMap.get(parentId).add(task);
            }
            
            for (PmTask rootTask : pageRootTasks) {
                addTaskToResult(result, rootTask, parentIdToChildrenMap);
            }
            
            page.setRecords(result);
        } else {
            page.setRecords(new java.util.ArrayList<>());
        }
        
        return page;
    }
    
    /**
     * 从完整任务列表中提取所有根任务（parentId为0的任务）
     * @param allTasks 所有任务列表
     * @return 根任务列表
     */
    private List<PmTask> buildFlatRootTasks(List<PmTask> allTasks) {
        List<PmTask> rootTasks = new java.util.ArrayList<>();
        Map<Long, List<PmTask>> parentIdToChildrenMap = new java.util.HashMap<>();
        
        for (PmTask task : allTasks) {
            Long parentId = task.getParentId() != null ? task.getParentId() : 0L;
            if (!parentIdToChildrenMap.containsKey(parentId)) {
                parentIdToChildrenMap.put(parentId, new java.util.ArrayList<>());
            }
            parentIdToChildrenMap.get(parentId).add(task);
        }
        
        List<PmTask> rawRootTasks = parentIdToChildrenMap.getOrDefault(0L, new java.util.ArrayList<>());
        rawRootTasks.sort((a, b) -> {
            int pinnedCompare = (b.getIsPinned() != null ? b.getIsPinned() : 0) - (a.getIsPinned() != null ? a.getIsPinned() : 0);
            if (pinnedCompare != 0) return pinnedCompare;
            
            int statusCompare = getStatusOrder(a.getStatus()) - getStatusOrder(b.getStatus());
            if (statusCompare != 0) return statusCompare;
            
            int priorityCompare = (a.getPriority() != null ? a.getPriority() : 999) - (b.getPriority() != null ? b.getPriority() : 999);
            if (priorityCompare != 0) return priorityCompare;
            
            return a.getCreateTime().compareTo(b.getCreateTime());
        });
        
        rootTasks.addAll(rawRootTasks);
        
        return rootTasks;
    }

    @Override
    public List<PmTask> getProjectTasks(Long projectId) {
        List<PmTask> allTasks = baseMapper.selectByProjectId(projectId);
        
        // 先修正所有任务的 taskLevel
        fixTaskLevels(allTasks);
        
        return buildTaskTree(allTasks);
    }

    /**
     * 修正所有任务的层级
     * @param allTasks 所有任务列表
     */
    private void fixTaskLevels(List<PmTask> allTasks) {
        // 构建 ID 到任务的映射
        Map<Long, PmTask> idToTaskMap = new java.util.HashMap<>();
        for (PmTask task : allTasks) {
            idToTaskMap.put(task.getId(), task);
        }
        
        // 递归修正每个任务的层级
        for (PmTask task : allTasks) {
            fixTaskLevel(task, idToTaskMap);
        }
    }
    
    /**
     * 修正单个任务的层级
     * @param task 任务
     * @param idToTaskMap ID 到任务的映射
     */
    private void fixTaskLevel(PmTask task, Map<Long, PmTask> idToTaskMap) {
        if (task.getParentId() == null || task.getParentId() == 0) {
            // 根任务，层级为 1
            task.setTaskLevel(1);
        } else {
            // 查找父任务
            PmTask parentTask = idToTaskMap.get(task.getParentId());
            if (parentTask != null) {
                // 确保父任务的层级已经修正
                if (parentTask.getTaskLevel() == null) {
                    fixTaskLevel(parentTask, idToTaskMap);
                }
                // 设置当前任务的层级为父任务层级 + 1
                task.setTaskLevel(parentTask.getTaskLevel() + 1);
            } else {
                // 找不到父任务，作为根任务处理
                task.setParentId(0L);
                task.setTaskLevel(1);
            }
        }
    }

    private List<PmTask> buildTaskTree(List<PmTask> allTasks) {
        List<PmTask> result = new java.util.ArrayList<>();
        Map<Long, List<PmTask>> parentIdToChildrenMap = new java.util.HashMap<>();
        
        for (PmTask task : allTasks) {
            Long parentId = task.getParentId() != null ? task.getParentId() : 0L;
            if (!parentIdToChildrenMap.containsKey(parentId)) {
                parentIdToChildrenMap.put(parentId, new java.util.ArrayList<>());
            }
            parentIdToChildrenMap.get(parentId).add(task);
        }
        
        List<PmTask> rootTasks = parentIdToChildrenMap.getOrDefault(0L, new java.util.ArrayList<>());
        rootTasks.sort((a, b) -> {
            int pinnedCompare = (b.getIsPinned() != null ? b.getIsPinned() : 0) - (a.getIsPinned() != null ? a.getIsPinned() : 0);
            if (pinnedCompare != 0) return pinnedCompare;
            
            int statusCompare = getStatusOrder(a.getStatus()) - getStatusOrder(b.getStatus());
            if (statusCompare != 0) return statusCompare;
            
            int priorityCompare = (a.getPriority() != null ? a.getPriority() : 999) - (b.getPriority() != null ? b.getPriority() : 999);
            if (priorityCompare != 0) return priorityCompare;
            
            return a.getCreateTime().compareTo(b.getCreateTime());
        });
        
        for (PmTask rootTask : rootTasks) {
            addTaskToResult(result, rootTask, parentIdToChildrenMap);
        }
        
        return result;
    }

    private int getStatusOrder(Integer status) {
        if (status == null) return 3;
        switch (status) {
            case 1: case 2: return 0;
            case 3: return 1;
            case 4: return 2;
            default: return 3;
        }
    }

    private void addTaskToResult(List<PmTask> result, PmTask task, Map<Long, List<PmTask>> parentIdToChildrenMap) {
        result.add(task);
        List<PmTask> children = parentIdToChildrenMap.getOrDefault(task.getId(), new java.util.ArrayList<>());
        children.sort((a, b) -> {
            int priorityCompare = (a.getPriority() != null ? a.getPriority() : 999) - (b.getPriority() != null ? b.getPriority() : 999);
            if (priorityCompare != 0) return priorityCompare;
            return a.getCreateTime().compareTo(b.getCreateTime());
        });
        for (PmTask child : children) {
            addTaskToResult(result, child, parentIdToChildrenMap);
        }
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
        
        // 如果前端没有传状态，默认为1（未开始）
        if (task.getStatus() == null || task.getStatus() == 0) {
            task.setStatus(1);
        }
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
        // 获取原任务数据
        PmTask oldTask = getById(task.getId());
        
        // 更新任务层级
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
        
        // 如果父级发生变化，更新所有子任务的层级
        if (oldTask != null && !task.getParentId().equals(oldTask.getParentId())) {
            updateSubTaskLevels(task.getId(), task.getTaskLevel());
        }
        
        // 根据状态自动调整进度
        if (task.getStatus() != null && oldTask != null) {
            Integer newStatus = task.getStatus();
            Integer oldStatus = oldTask.getStatus();
            
            // 只有当状态发生变化时才调整进度
            if (!newStatus.equals(oldStatus)) {
                switch (newStatus) {
                    case 1: // 未开始
                        task.setProgress(0);
                        break;
                    case 3: // 已完成
                        task.setProgress(100);
                        break;
                    case 2: // 进行中
                    case 4: // 已暂停
                    default:
                        // 进行中和已暂停状态，保留原进度
                        if (task.getProgress() == null) {
                            task.setProgress(oldTask.getProgress());
                        }
                        break;
                }
            }
        }
        
        task.setUpdateTime(LocalDateTime.now());
        updateById(task);
        
        updateProjectProgress(task.getProjectId());
        
        log.info("更新任务成功: {}", task.getId());
        return task;
    }
    
    /**
     * 更新子任务层级
     * @param parentId 父任务ID
     * @param parentLevel 父任务层级
     */
    private void updateSubTaskLevels(Long parentId, Integer parentLevel) {
        LambdaQueryWrapper<PmTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmTask::getParentId, parentId);
        List<PmTask> subTasks = list(wrapper);
        
        for (PmTask subTask : subTasks) {
            subTask.setTaskLevel(parentLevel + 1);
            subTask.setUpdateTime(LocalDateTime.now());
            updateById(subTask);
            
            // 递归更新子任务的子任务
            updateSubTaskLevels(subTask.getId(), subTask.getTaskLevel());
        }
    }

    @Override
    @Transactional
    public void updateProgress(Long taskId, Integer progress) {
        PmTask task = getById(taskId);
        if (task != null) {
            task.setProgress(progress);
            task.setUpdateTime(LocalDateTime.now());
            
            // 根据进度自动设置状态
            if (progress >= 100) {
                task.setStatus(3); // 已完成
            } else if (progress <= 0) {
                task.setStatus(1); // 未开始
            } else {
                task.setStatus(2); // 进行中
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
        wrapper.eq(PmTask::getProjectId, projectId);
        wrapper.eq(PmTask::getParentId, 0L);
        
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
        if (task.getStatus() != null && task.getStatus() == 3) {
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
    @Transactional
    public void recalculateAllProjectProgress() {
        LambdaQueryWrapper<PmProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmProject::getArchived, 0);
        List<PmProject> projects = projectMapper.selectList(wrapper);
        
        for (PmProject project : projects) {
            updateProjectProgress(project.getId());
        }
        
        log.info("重新计算所有项目进度完成，共处理 {} 个项目", projects.size());
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
        
        // 检查是否是项目的所有者或代理负责人
        PmProject project = projectMapper.selectById(task.getProjectId());
        if (project != null) {
            if (project.getOwnerId() != null && project.getOwnerId().equals(employeeId)) {
                return true;
            }
            // 检查是否是代理负责人之一
            List<Long> deputyOwnerIds = pmProjectDeputyOwnerService.getDeputyOwnerIdsByProjectId(task.getProjectId());
            if (deputyOwnerIds != null && deputyOwnerIds.contains(employeeId)) {
                return true;
            }
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
        
        // 检查是否是项目的所有者或代理负责人
        PmProject project = projectMapper.selectById(task.getProjectId());
        if (project != null) {
            if (project.getOwnerId() != null && project.getOwnerId().equals(employeeId)) {
                return true;
            }
            // 检查是否是代理负责人之一
            List<Long> deputyOwnerIds = pmProjectDeputyOwnerService.getDeputyOwnerIdsByProjectId(task.getProjectId());
            if (deputyOwnerIds != null && deputyOwnerIds.contains(employeeId)) {
                return true;
            }
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

    @Override
    public Page<WorkloadDTO> getWorkloadPage(Integer pageNum, Integer pageSize, Long projectId, String taskName, Long assigneeId, LocalDate taskStartDate, LocalDate taskEndDate, LocalDateTime bindStartTime, LocalDateTime bindEndTime) {
        Page<WorkloadDTO> resultPage = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<PmTask> taskWrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            taskWrapper.eq(PmTask::getProjectId, projectId);
        }
        if (taskName != null && !taskName.isEmpty()) {
            taskWrapper.like(PmTask::getTaskName, taskName);
        }
        if (assigneeId != null) {
            taskWrapper.eq(PmTask::getAssigneeId, assigneeId);
        }
        if (taskStartDate != null) {
            taskWrapper.ge(PmTask::getStartDate, taskStartDate);
        }
        if (taskEndDate != null) {
            taskWrapper.le(PmTask::getEndDate, taskEndDate);
        }
        taskWrapper.orderByDesc(PmTask::getCreateTime);

        List<PmTask> allTasks = list(taskWrapper);

        List<Long> taskIds = allTasks.stream().map(PmTask::getId).collect(Collectors.toList());

        Map<Long, List<PmTaskCustomRow>> taskBindingsMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            LambdaQueryWrapper<PmTaskCustomRow> bindingWrapper = new LambdaQueryWrapper<>();
            bindingWrapper.in(PmTaskCustomRow::getTaskId, taskIds);
            if (bindStartTime != null) {
                bindingWrapper.ge(PmTaskCustomRow::getCreateTime, bindStartTime);
            }
            if (bindEndTime != null) {
                bindingWrapper.le(PmTaskCustomRow::getCreateTime, bindEndTime);
            }
            List<PmTaskCustomRow> bindings = taskCustomRowMapper.selectList(bindingWrapper);
            taskBindingsMap = bindings.stream().collect(Collectors.groupingBy(PmTaskCustomRow::getTaskId));
        }

        List<Long> projectIds = allTasks.stream().map(PmTask::getProjectId).distinct().collect(Collectors.toList());
        Map<Long, PmProject> projectMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            List<PmProject> projects = projectMapper.selectBatchIds(projectIds);
            projectMap = projects.stream().collect(Collectors.toMap(PmProject::getId, p -> p));
        }

        List<Long> employeeIds = allTasks.stream().map(PmTask::getAssigneeId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, SysEmployee> employeeMap = new HashMap<>();
        if (!employeeIds.isEmpty()) {
            LambdaQueryWrapper<SysEmployee> employeeWrapper = new LambdaQueryWrapper<>();
            employeeWrapper.in(SysEmployee::getId, employeeIds);
            List<SysEmployee> employees = sysEmployeeMapper.selectList(employeeWrapper);
            employeeMap = employees.stream().collect(Collectors.toMap(SysEmployee::getId, e -> e));
        }

        List<WorkloadDTO> workloadList = new ArrayList<>();
        for (PmTask task : allTasks) {
            WorkloadDTO dto = new WorkloadDTO();
            dto.setId(task.getId());
            dto.setProjectId(task.getProjectId());
            dto.setTaskId(task.getId());
            dto.setTaskName(task.getTaskName());
            dto.setTaskStatus(task.getStatus());
            dto.setTaskStatusText(getTaskStatusText(task.getStatus()));
            dto.setStartDate(task.getStartDate());
            dto.setEndDate(task.getEndDate());
            dto.setProgress(task.getProgress());
            dto.setAssigneeId(task.getAssigneeId());

            PmProject project = projectMap.get(task.getProjectId());
            if (project != null) {
                dto.setProjectName(project.getProjectName());
            }

            SysEmployee employee = employeeMap.get(task.getAssigneeId());
            if (employee != null) {
                dto.setAssigneeName(employee.getEmployeeName());
            }

            List<PmTaskCustomRow> bindings = taskBindingsMap.getOrDefault(task.getId(), new ArrayList<>());
            List<WorkloadDTO.BindingInfo> bindingInfos = new ArrayList<>();
            for (PmTaskCustomRow binding : bindings) {
                WorkloadDTO.BindingInfo bindingInfo = new WorkloadDTO.BindingInfo();
                bindingInfo.setId(binding.getId());
                bindingInfo.setTableId(binding.getTableId());
                bindingInfo.setRowId(binding.getRowId());
                bindingInfo.setOrderNo(binding.getOrderNo());
                bindingInfo.setCreateTime(binding.getCreateTime());

                PmCustomTable table = customTableMapper.selectById(binding.getTableId());
                if (table != null) {
                    bindingInfo.setTableName(table.getTableName());
                }

                PmCustomTableRow row = customTableRowMapper.selectById(binding.getRowId());
                if (row != null && row.getRowData() != null) {
                    try {
                        Map<String, Object> rowDataMap = objectMapper.readValue(row.getRowData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        bindingInfo.setRowData(rowDataMap);
                    } catch (Exception e) {
                        log.error("解析行数据失败", e);
                    }
                }

                bindingInfos.add(bindingInfo);
            }
            dto.setBindings(bindingInfos);

            workloadList.add(dto);
        }

        int total = workloadList.size();
        resultPage.setTotal(total);

        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        if (fromIndex < total) {
            resultPage.setRecords(workloadList.subList(fromIndex, toIndex));
        } else {
            resultPage.setRecords(new ArrayList<>());
        }

        return resultPage;
    }

    private String getTaskStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "未开始";
            case 2: return "进行中";
            case 3: return "已完成";
            case 4: return "已暂停";
            default: return "未知";
        }
    }
}
