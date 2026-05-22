package com.lasu.hyperduty.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.pm.dto.PmShadowAnnotationVO;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationWithProjectVO;
import com.lasu.hyperduty.pm.dto.ShadowTaskCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskUpdateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskVO;
import com.lasu.hyperduty.pm.entity.PmProject;
import com.lasu.hyperduty.pm.entity.PmShadowAnnotation;
import com.lasu.hyperduty.pm.entity.PmTask;
import com.lasu.hyperduty.pm.entity.PmTaskShadow;
import com.lasu.hyperduty.pm.mapper.PmProjectMapper;
import com.lasu.hyperduty.pm.mapper.PmShadowAnnotationMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskShadowMapper;
import com.lasu.hyperduty.pm.service.PmProjectDeputyOwnerService;
import com.lasu.hyperduty.pm.service.PmTaskShadowService;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 影子任务 Service 实现 (v2)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PmTaskShadowServiceImpl implements PmTaskShadowService {

    private final PmTaskShadowMapper taskShadowMapper;
    private final PmTaskMapper taskMapper;
    private final PmShadowAnnotationMapper annotationMapper;
    private final PmProjectMapper projectMapper;
    private final PmProjectDeputyOwnerService pmProjectDeputyOwnerService;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        shadow.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
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

        if (dto.getParentId() != null) {
            shadow.setParentId(dto.getParentId());
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
    public Page<ShadowTaskVO> pageTaskListWithShadows(Integer pageNum, Integer pageSize, Long projectId, String taskName, String assigneeName, Integer status, Integer priority, Long currentEmployeeId) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        Page<ShadowTaskVO> page = new Page<>(pageNum, pageSize);

        // 1. 查询所有符合条件的任务（真实任务 + 影子任务）
        List<ShadowTaskVO> allTasks = taskShadowMapper.selectTaskListWithShadows(projectId);

        // 2. 应用过滤条件
        if (taskName != null && !taskName.isEmpty()) {
            allTasks = allTasks.stream()
                    .filter(task -> task.getTaskName() != null && task.getTaskName().toLowerCase().contains(taskName.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (assigneeName != null && !assigneeName.isEmpty()) {
            allTasks = allTasks.stream()
                    .filter(task -> task.getOwnerName() != null && task.getOwnerName().toLowerCase().contains(assigneeName.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (status != null) {
            allTasks = allTasks.stream()
                    .filter(task -> task.getStatus() != null && task.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        if (priority != null) {
            allTasks = allTasks.stream()
                    .filter(task -> task.getPriority() != null && task.getPriority().equals(priority))
                    .collect(Collectors.toList());
        }

        // 3. 设置权限
        Long employeeId = currentEmployeeId != null ? currentEmployeeId : getEmployeeIdByUsername(currentUsername);
        for (ShadowTaskVO task : allTasks) {
            if (task.getIsShadow() != null && task.getIsShadow() == 1) {
                // 影子任务
                PmTaskShadow shadow = taskShadowMapper.selectById(task.getId());
                if (shadow != null) {
                    boolean isOwner = shadow.getCreatedBy() != null && currentUsername != null && shadow.getCreatedBy().equals(currentUsername);
                    task.setHasPermission(isOwner);
                    task.setHasDeletePermission(isOwner);
                }
            } else {
                // 真实任务
                boolean hasPermission = hasRealTaskPermission(task.getId(), employeeId);
                boolean hasDeletePermission = hasRealTaskDeletePermission(task.getId(), employeeId);
                task.setHasPermission(hasPermission);
                task.setHasDeletePermission(hasDeletePermission);
            }
        }

        // 4. 构建父子关系映射
        Map<Long, List<ShadowTaskVO>> parentIdToChildrenMap = new HashMap<>();
        for (ShadowTaskVO task : allTasks) {
            Long parentId = task.getParentId() != null ? task.getParentId() : 0L;
            if (!parentIdToChildrenMap.containsKey(parentId)) {
                parentIdToChildrenMap.put(parentId, new ArrayList<>());
            }
            parentIdToChildrenMap.get(parentId).add(task);
        }

        // 5. 获取并排序根任务 - 先分组再合并
        List<ShadowTaskVO> rawRootTasks = parentIdToChildrenMap.getOrDefault(0L, new ArrayList<>());
        
        // 分组：先分成4组
        List<ShadowTaskVO> pinnedInProgress = new ArrayList<>(); // 置顶的未开始/进行中
        List<ShadowTaskVO> pinnedCompleted = new ArrayList<>();   // 置顶的已完成/已暂停
        List<ShadowTaskVO> unpinnedInProgress = new ArrayList<>(); // 非置顶的未开始/进行中
        List<ShadowTaskVO> unpinnedCompleted = new ArrayList<>();  // 非置顶的已完成/已暂停
        
        for (ShadowTaskVO task : rawRootTasks) {
            int isPinned = (task.getIsPinned() != null) ? task.getIsPinned() : 0;
            int statusOrder = getStatusOrder(task.getStatus());
            
            if (isPinned == 1) {
                if (statusOrder == 0) {
                    pinnedInProgress.add(task);
                } else {
                    pinnedCompleted.add(task);
                }
            } else {
                if (statusOrder == 0) {
                    unpinnedInProgress.add(task);
                } else {
                    unpinnedCompleted.add(task);
                }
            }
        }
        
        // 在每个组内按优先级和创建时间排序
        java.util.Comparator<ShadowTaskVO> groupComparator = (a, b) -> {
            int priA = (a.getPriority() != null) ? a.getPriority() : 999;
            int priB = (b.getPriority() != null) ? b.getPriority() : 999;
            if (priA != priB) {
                return priA - priB;
            }
            if (a.getCreateTime() != null && b.getCreateTime() != null) {
                return a.getCreateTime().compareTo(b.getCreateTime());
            }
            return 0;
        };
        
        pinnedInProgress.sort(groupComparator);
        pinnedCompleted.sort(groupComparator);
        unpinnedInProgress.sort(groupComparator);
        unpinnedCompleted.sort(groupComparator);
        
        // 按照顺序合并
        List<ShadowTaskVO> rootTasks = new ArrayList<>();
        rootTasks.addAll(pinnedInProgress);
        rootTasks.addAll(pinnedCompleted);
        rootTasks.addAll(unpinnedInProgress);
        rootTasks.addAll(unpinnedCompleted);

        // 6. 确定每个根任务在扁平列表中的起始和结束索引
        List<RootTaskRange> rootTaskRanges = new ArrayList<>();
        int currentIndex = 0;
        for (ShadowTaskVO rootTask : rootTasks) {
            int count = countTaskAndChildren(rootTask, parentIdToChildrenMap);
            rootTaskRanges.add(new RootTaskRange(rootTask, currentIndex, currentIndex + count));
            currentIndex += count;
        }

        // 7. 预先计算好每页应该显示哪些根任务
        List<List<RootTaskRange>> pagesRootTasks = new ArrayList<>();
        List<RootTaskRange> currentPageRootTasks = new ArrayList<>();
        int currentPageTaskCount = 0;

        for (RootTaskRange range : rootTaskRanges) {
            int taskCount = range.endIndex - range.startIndex;
            boolean hasChildren = taskCount > 1; // 这个根任务是否有子任务

            // 如果当前页是空的，直接添加这个根任务（不管它有多少子任务）
            if (currentPageRootTasks.isEmpty()) {
                currentPageRootTasks.add(range);
                currentPageTaskCount += taskCount;
            } else {
                // 检查当前页是否都是根任务（没有子任务）
                boolean currentPageAllRootTasks = true;
                for (RootTaskRange existingRange : currentPageRootTasks) {
                    int existingTaskCount = existingRange.endIndex - existingRange.startIndex;
                    if (existingTaskCount > 1) {
                        currentPageAllRootTasks = false;
                        break;
                    }
                }

                if (currentPageAllRootTasks && !hasChildren) {
                    // 都是根任务，且当前要添加的也是根任务：按pageSize严格分页
                    if (currentPageTaskCount + 1 > pageSize) {
                        // 超过pageSize，保存当前页，新根任务从新页开始
                        pagesRootTasks.add(new ArrayList<>(currentPageRootTasks));
                        currentPageRootTasks.clear();
                        currentPageRootTasks.add(range);
                        currentPageTaskCount = taskCount;
                    } else {
                        // 不超过，添加到当前页
                        currentPageRootTasks.add(range);
                        currentPageTaskCount += taskCount;
                    }
                } else {
                    // 有子任务的情况：检查添加这个根任务自己（+1条）会不会超过pageSize
                    if (currentPageTaskCount + 1 > pageSize) {
                        // 会超过！保存当前页，然后新根任务从新页开始
                        pagesRootTasks.add(new ArrayList<>(currentPageRootTasks));

                        // 新开一页，添加这个根任务（它的所有子任务都在这一页）
                        currentPageRootTasks.clear();
                        currentPageRootTasks.add(range);
                        currentPageTaskCount = taskCount;
                    } else {
                        // 不会超过，添加到当前页，这个根任务及其所有子任务都在当前页
                        currentPageRootTasks.add(range);
                        currentPageTaskCount += taskCount;
                    }
                }
            }
        }

        // 添加最后一页
        if (!currentPageRootTasks.isEmpty()) {
            pagesRootTasks.add(currentPageRootTasks);
        }

        // 8. 调整total使得前端显示的总页数等于pagesRootTasks.size()
        int adjustedTotal = pagesRootTasks.size() * pageSize;
        page.setTotal(adjustedTotal);

        // 9. 获取当前页的根任务
        List<ShadowTaskVO> pageRootTasks = new ArrayList<>();
        if (pageNum <= pagesRootTasks.size()) {
            List<RootTaskRange> targetPageRanges = pagesRootTasks.get(pageNum - 1);
            for (RootTaskRange range : targetPageRanges) {
                pageRootTasks.add(range.rootTask);
            }
        }

        // 10. 构建结果列表
        List<ShadowTaskVO> result = new ArrayList<>();
        for (ShadowTaskVO rootTask : pageRootTasks) {
            addTaskToResult(result, rootTask, parentIdToChildrenMap);
        }
        page.setRecords(result);

        return page;
    }
    
    /**
     * 获取状态排序值
     */
    private int getStatusOrder(Integer status) {
        if (status == null) {
            return 3;
        }
        switch (status) {
            case 1: // 未开始
            case 2: // 进行中
                return 0;
            case 3: // 已完成
                return 1;
            case 4: // 已暂停
                return 2;
            default:
                return 3;
        }
    }

    @Override
    public List<ShadowTaskVO> getTaskListWithShadows(Long projectId, Long currentEmployeeId) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        Long employeeId = currentEmployeeId != null ? currentEmployeeId : getEmployeeIdByUsername(currentUsername);
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
                // 真实任务
                boolean hasPermission = hasRealTaskPermission(task.getId(), employeeId);
                boolean hasDeletePermission = hasRealTaskDeletePermission(task.getId(), employeeId);
                task.setHasPermission(hasPermission);
                task.setHasDeletePermission(hasDeletePermission);
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
    public List<PmShadowAnnotationVO> getAnnotationsByShadowId(Long shadowId) {
        return annotationMapper.selectByShadowId(shadowId);
    }

    @Override
    public List<ShadowAnnotationWithProjectVO> getAnnotationsBySourceTaskId(Long sourceTaskId) {
        return annotationMapper.selectBySourceTaskId(sourceTaskId);
    }

    // ========================================
    // 辅助方法
    // ========================================

    /**
     * 计算任务及其所有子任务的总条数
     */
    private int countTaskAndChildren(ShadowTaskVO task, Map<Long, List<ShadowTaskVO>> parentIdToChildrenMap) {
        int count = 1; // 当前任务自己
        List<ShadowTaskVO> children = parentIdToChildrenMap.getOrDefault(task.getId(), new ArrayList<>());
        for (ShadowTaskVO child : children) {
            count += countTaskAndChildren(child, parentIdToChildrenMap);
        }
        return count;
    }

    /**
     * 递归添加任务及其子任务到结果
     */
    private void addTaskToResult(List<ShadowTaskVO> result, ShadowTaskVO task, Map<Long, List<ShadowTaskVO>> parentIdToChildrenMap) {
        result.add(task);
        List<ShadowTaskVO> children = parentIdToChildrenMap.getOrDefault(task.getId(), new ArrayList<>());
        // 对子任务排序
        children.sort((a, b) -> {
            int priorityCompare = (a.getPriority() != null ? a.getPriority() : 999) - (b.getPriority() != null ? b.getPriority() : 999);
            if (priorityCompare != 0) return priorityCompare;
            if (a.getCreateTime() != null && b.getCreateTime() != null) {
                return a.getCreateTime().compareTo(b.getCreateTime());
            }
            return 0;
        });
        for (ShadowTaskVO child : children) {
            addTaskToResult(result, child, parentIdToChildrenMap);
        }
    }

    /**
     * 根任务范围类，记录根任务在扁平列表中的起始和结束索引
     */
    private static class RootTaskRange {
        ShadowTaskVO rootTask;
        int startIndex;
        int endIndex;

        RootTaskRange(ShadowTaskVO rootTask, int startIndex, int endIndex) {
            this.rootTask = rootTask;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    // ========================================
    // 权限判断方法
    // ========================================

    /**
     * 判断当前用户是否有真实任务的编辑权限
     */
    private boolean hasRealTaskPermission(Long taskId, Long currentEmployeeId) {
        if (currentEmployeeId == null) {
            return false;
        }
        PmTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }

        if (task.getCreateBy() != null && task.getCreateBy().equals(currentEmployeeId)) {
            return true;
        }
        if (task.getAssigneeId() != null && task.getAssigneeId().equals(currentEmployeeId)) {
            return true;
        }

        PmProject project = projectMapper.selectById(task.getProjectId());
        if (project != null) {
            if (project.getOwnerId() != null && project.getOwnerId().equals(currentEmployeeId)) {
                return true;
            }
            List<Long> deputyOwnerIds = pmProjectDeputyOwnerService.getDeputyOwnerIdsByProjectId(task.getProjectId());
            if (deputyOwnerIds != null && deputyOwnerIds.contains(currentEmployeeId)) {
                return true;
            }
        }

        if (project != null && project.getParticipants() != null) {
            List<Long> participants = project.getParticipants();
            if (participants.contains(currentEmployeeId)) {
                return true;
            }
        }

        if (task.getStakeholders() != null) {
            String stakeholders = task.getStakeholders();
            try {
                List<Long> stakeholderList = objectMapper.readValue(stakeholders, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
                if (stakeholderList.contains(currentEmployeeId)) {
                    return true;
                }
            } catch (Exception e) {
                if (stakeholders.contains("[" + currentEmployeeId + "]") || 
                    stakeholders.contains("," + currentEmployeeId + ",") || 
                    stakeholders.startsWith(currentEmployeeId + ",") || 
                    stakeholders.endsWith("," + currentEmployeeId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断当前用户是否有真实任务的删除权限
     */
    private boolean hasRealTaskDeletePermission(Long taskId, Long currentEmployeeId) {
        if (currentEmployeeId == null) {
            return false;
        }
        PmTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }

        if (task.getCreateBy() != null && task.getCreateBy().equals(currentEmployeeId)) {
            return true;
        }
        if (task.getAssigneeId() != null && task.getAssigneeId().equals(currentEmployeeId)) {
            return true;
        }

        PmProject project = projectMapper.selectById(task.getProjectId());
        if (project != null) {
            if (project.getOwnerId() != null && project.getOwnerId().equals(currentEmployeeId)) {
                return true;
            }
            List<Long> deputyOwnerIds = pmProjectDeputyOwnerService.getDeputyOwnerIdsByProjectId(task.getProjectId());
            if (deputyOwnerIds != null && deputyOwnerIds.contains(currentEmployeeId)) {
                return true;
            }
        }

        if (project != null && project.getParticipants() != null) {
            List<Long> participants = project.getParticipants();
            if (participants.contains(currentEmployeeId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 根据用户名获取员工ID
     */
    private Long getEmployeeIdByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployee::getUsername, username);
        SysEmployee employee = sysEmployeeMapper.selectOne(wrapper);
        return employee != null ? employee.getId() : null;
    }
}
