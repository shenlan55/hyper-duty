package com.lasu.hyperduty.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.pm.dto.*;
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
import java.util.*;
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

        // ===== 1. 一次查项目下所有根任务（真实 + 影子 UNION ALL，无分页） =====
        List<ShadowTaskVO> allRoots = taskShadowMapper.selectAllRootTasksWithShadows(projectId);
        if (allRoots.isEmpty()) {
            page.setTotal(0L);
            page.setRecords(new ArrayList<>());
            return page;
        }

        // ===== 2. 一次查项目下所有子任务（按行分页用，根任务不可切断） =====
        List<ShadowTaskVO> allSubTasks = taskShadowMapper.selectAllSubTasksWithShadows(projectId);

        // ===== 3. 按 parentId 统计每个根的子数 =====
        Map<Long, Integer> subCountMap = new HashMap<>();
        for (ShadowTaskVO sub : allSubTasks) {
            Long parentId = sub.getParentId() != null ? sub.getParentId() : 0L;
            subCountMap.merge(parentId, 1, Integer::sum);
        }

        // ===== 4. 计算每个根的"行数"（1 + 子数） =====
        int totalRoots = allRoots.size();
        int[] rowCountOfRoot = new int[totalRoots];
        for (int i = 0; i < totalRoots; i++) {
            rowCountOfRoot[i] = 1 + subCountMap.getOrDefault(allRoots.get(i).getId(), 0);
        }
        int totalRows = 0;
        for (int c : rowCountOfRoot) totalRows += c;

        // ===== 5. "根不可切断"分页算法（与 PmTaskServiceImpl 原 pageList 同款） =====
        // 规则：每页装满 pageSize 行（根不可切断）→ 累加每个根的行数，超 pageSize 就开新页
        //       如果一个根本身就 > pageSize（行数很多），则单独占 1 页
        int[] pageOfRoot = new int[totalRoots]; // 每个根归属的页码（1-based）
        int currentPageRows = 0;
        int currentPage = 1;
        for (int i = 0; i < totalRoots; i++) {
            int rc = rowCountOfRoot[i];
            if (currentPageRows == 0) {
                // 当前页空，直接放这个根（即使超 pageSize 也放这里——保证不切断）
                pageOfRoot[i] = currentPage;
                currentPageRows += rc;
            } else if (currentPageRows + rc > pageSize) {
                // 当前页非空，加上这个根会超 pageSize → 开新页
                currentPage++;
                pageOfRoot[i] = currentPage;
                currentPageRows = rc;
            } else {
                // 不超，添加到当前页
                pageOfRoot[i] = currentPage;
                currentPageRows += rc;
            }
        }
        int totalPages = currentPage;

        // ===== 6. 找出当前页的根范围 =====
        int fromRootIdx = -1, toRootIdx = -1;
        for (int i = 0; i < totalRoots; i++) {
            if (pageOfRoot[i] == pageNum) {
                if (fromRootIdx == -1) fromRootIdx = i;
                toRootIdx = i;
            }
        }
        if (fromRootIdx == -1) {
            // 超出范围（pageNum 超过总页数）
            page.setTotal((long) totalRows);
            page.setRecords(new ArrayList<>());
            return page;
        }

        // ===== 7. 拼装当前页根 + 子 =====
        List<ShadowTaskVO> pageRoots = allRoots.subList(fromRootIdx, toRootIdx + 1);
        Set<Long> pageRootIds = pageRoots.stream().map(ShadowTaskVO::getId).collect(Collectors.toSet());
        List<ShadowTaskVO> pageSubs = allSubTasks.stream()
                .filter(s -> pageRootIds.contains(s.getParentId() != null ? s.getParentId() : 0L))
                .collect(Collectors.toList());

        // ===== 8. 按 parentId 构建父子关系映射 =====
        Map<Long, List<ShadowTaskVO>> parentIdToChildrenMap = new HashMap<>();
        for (ShadowTaskVO sub : pageSubs) {
            Long parentId = sub.getParentId() != null ? sub.getParentId() : 0L;
            parentIdToChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(sub);
        }

        // ===== 9. 设置权限 =====
        Long employeeId = currentEmployeeId != null ? currentEmployeeId : getEmployeeIdByUsername(currentUsername);
        for (ShadowTaskVO task : pageRoots) {
            applyTaskPermission(task, currentUsername, employeeId);
        }
        for (ShadowTaskVO task : pageSubs) {
            applyTaskPermission(task, currentUsername, employeeId);
        }

        // ===== 10. 拼装：根 + 跟随的子任务（树形扁平展开） =====
        List<ShadowTaskVO> result = new ArrayList<>();
        for (ShadowTaskVO rootTask : pageRoots) {
            addTaskToResult(result, rootTask, parentIdToChildrenMap);
        }

        // ===== 11. 调整 total：让前端 el-pagination 显示正确的页数 =====
        // 前端 pageSize × 实际页数 = 应显示的 total（如 2 页 × 10/页 = 20）
        page.setTotal((long) totalPages * pageSize);
        page.setRecords(result);
        return page;
    }

    /**
     * 单个任务权限赋值（与旧逻辑一致，保留 N+1 优化空间）
     */
    private void applyTaskPermission(ShadowTaskVO task, String currentUsername, Long employeeId) {
        if (task.getIsShadow() != null && task.getIsShadow() == 1) {
            PmTaskShadow shadow = taskShadowMapper.selectById(task.getId());
            if (shadow != null) {
                boolean isOwner = shadow.getCreatedBy() != null
                        && currentUsername != null
                        && shadow.getCreatedBy().equals(currentUsername);
                task.setHasPermission(isOwner);
                task.setHasDeletePermission(isOwner);
            }
        } else {
            task.setHasPermission(hasRealTaskPermission(task.getId(), employeeId));
            task.setHasDeletePermission(hasRealTaskDeletePermission(task.getId(), employeeId));
        }
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
        List<ShadowTaskVO> tasks = taskShadowMapper.selectAllRootTasksWithShadows(projectId);
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
