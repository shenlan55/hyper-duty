package com.lasu.hyperduty.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.common.dto.WorkloadDTO;
import com.lasu.hyperduty.common.service.AttachmentService;
import com.lasu.hyperduty.pm.dto.BatchTaskCreateDTO;
import com.lasu.hyperduty.pm.dto.TaskCreateDTO;
import com.lasu.hyperduty.pm.dto.TaskQueryDTO;
import com.lasu.hyperduty.pm.dto.TaskUpdateDTO;
import com.lasu.hyperduty.pm.dto.TaskVO;
import com.lasu.hyperduty.pm.entity.PmCustomTable;
import com.lasu.hyperduty.pm.entity.PmCustomTableRow;
import com.lasu.hyperduty.pm.entity.PmProject;
import com.lasu.hyperduty.pm.entity.PmTask;
import com.lasu.hyperduty.pm.entity.PmTaskCustomRow;
import com.lasu.hyperduty.pm.mapper.PmCustomTableColumnMapper;
import com.lasu.hyperduty.pm.mapper.PmCustomTableMapper;
import com.lasu.hyperduty.pm.mapper.PmCustomTableRowMapper;
import com.lasu.hyperduty.pm.mapper.PmProjectMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskCustomRowMapper;
import com.lasu.hyperduty.pm.mapper.PmTaskMapper;
import com.lasu.hyperduty.pm.service.PmProjectDeputyOwnerService;
import com.lasu.hyperduty.pm.service.PmTaskService;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;











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
    private final AttachmentService attachmentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<PmTask> pageList(Integer pageNum, Integer pageSize, Long projectId, Long assigneeId, Integer status, Integer priority, String taskName, String assigneeName) {
        Page<PmTask> page = new Page<>(pageNum, pageSize);

        // ===== 1. 根任务总数（SQL COUNT） =====
        Long total = baseMapper.countRootTask(projectId, assigneeId, status, priority, taskName, assigneeName);
        page.setTotal(total == null ? 0L : total);
        if (page.getTotal() == 0) {
            page.setRecords(new ArrayList<>());
            return page;
        }

        // ===== 2. 当前页根任务（SQL 真分页，LIMIT/OFFSET） =====
        int offset = (pageNum - 1) * pageSize;
        List<PmTask> rootTasks = baseMapper.selectRootTaskPage(
                projectId, assigneeId, status, priority, taskName, assigneeName, offset, pageSize);
        if (rootTasks.isEmpty()) {
            page.setRecords(new ArrayList<>());
            return page;
        }

        // ===== 3. 一次性拉取所有根的子树（保持"根任务+完整子树一页"语义） =====
        List<Long> rootIds = rootTasks.stream().map(PmTask::getId).collect(Collectors.toList());
        List<PmTask> subTasks = baseMapper.selectSubTasksByRootIds(rootIds, projectId);

        // ===== 4. 按 parentId 构建父子关系映射 =====
        Map<Long, List<PmTask>> parentIdToChildrenMap = new HashMap<>();
        for (PmTask sub : subTasks) {
            Long parentId = sub.getParentId() != null ? sub.getParentId() : 0L;
            parentIdToChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(sub);
        }

        // ===== 5. 拼装：根 + 跟随的子任务（树形扁平展开） =====
        List<PmTask> result = new ArrayList<>();
        for (PmTask rootTask : rootTasks) {
            addTaskToResult(result, rootTask, parentIdToChildrenMap);
        }

        // ===== 6. 批量注入最后进度更新时间（替代原 LATERAL N+1） =====
        injectLastProgressUpdateTime(result);

        // ===== 7. 附件处理（保留原逻辑） =====
        result = attachmentService.ensureAttachmentsForTaskList(result);

        page.setRecords(result);
        return page;
    }

    /**
     * 批量预取每个任务的最后进度更新时间，注入到 PmTask.lastProgressUpdateTime
     * 2026-06-22 优化：替代 LATERAL 子查询，避免 N+1
     */
    private void injectLastProgressUpdateTime(List<PmTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<Long> taskIds = tasks.stream().map(PmTask::getId).collect(Collectors.toList());
        List<Map<String, Object>> progressList = baseMapper.selectLastProgressTimesByTaskIds(taskIds);
        if (progressList == null || progressList.isEmpty()) {
            return;
        }
        Map<Long, LocalDateTime> progressMap = new HashMap<>(progressList.size() * 2);
        for (Map<String, Object> row : progressList) {
            Object tid = row.get("task_id");
            Object time = row.get("last_progress_update_time");
            if (tid != null && time != null) {
                progressMap.put(((Number) tid).longValue(), toLocalDateTime(time));
            }
        }
        for (PmTask task : tasks) {
            if (task.getId() != null) {
                task.setLastProgressUpdateTime(progressMap.get(task.getId()));
            }
        }
    }

    /**
     * 把 Object 转 LocalDateTime（PG 的 Timestamp/LocalDateTime 都可能）
     */
    private static LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime) return (LocalDateTime) v;
        if (v instanceof java.sql.Timestamp) return ((java.sql.Timestamp) v).toLocalDateTime();
        if (v instanceof java.util.Date) return ((java.util.Date) v).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        try { return LocalDateTime.parse(v.toString()); } catch (Exception e) { return null; }
    }
    
    /**
     * 根任务范围类，记录根任务在扁平列表中的起始和结束索引
     */
    private static class RootTaskRange {
        PmTask rootTask;
        int startIndex;
        int endIndex;
        
        RootTaskRange(PmTask rootTask, int startIndex, int endIndex) {
            this.rootTask = rootTask;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
    
    /**
     * 计算任务及其所有子任务的总条数
     */
    private int countTaskAndChildren(PmTask task, Map<Long, List<PmTask>> parentIdToChildrenMap) {
        int count = 1; // 当前任务本身
        List<PmTask> children = parentIdToChildrenMap.getOrDefault(task.getId(), new java.util.ArrayList<>());
        for (PmTask child : children) {
            count += countTaskAndChildren(child, parentIdToChildrenMap);
        }
        return count;
    }

    
    /**
     * 从完整任务列表中提取所有根任务（parentId为0的任务）
     * @param allTasks 所有任务列表
     * @return 根任务列表
     */
    private List<PmTask> buildFlatRootTasks(List<PmTask> allTasks) {
        List<PmTask> rootTasks = new java.util.ArrayList<>();
        Map<Long, List<PmTask>> parentIdToChildrenMap = new HashMap<>();
        
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
        Map<Long, PmTask> idToTaskMap = new HashMap<>();
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
        Map<Long, List<PmTask>> parentIdToChildrenMap = new HashMap<>();
        
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
    public List<PmTask> getMyTasks(Long employeeId, String taskName) {
        return baseMapper.selectMyTasks(employeeId, taskName);
    }

    @Override
    public List<PmTask> getMyTasksByProject(Long employeeId, Long projectId, String taskName) {
        return baseMapper.selectMyTasksByProject(employeeId, projectId, taskName);
    }

    @Override
    public Map<String, Object> getMyTaskStats(Long employeeId, Long projectId, String taskName) {
        Map<String, Object> raw = baseMapper.selectMyTaskStats(employeeId, projectId, taskName);
        if (raw == null) {
            return new HashMap<>();
        }
        // 字段名转 long（PG COUNT 返回 bigint）
        Map<String, Object> result = new HashMap<>();
        result.put("total", toLong(raw.get("total")));
        result.put("pending", toLong(raw.get("pending")));
        result.put("inProgress", toLong(raw.get("in_progress")));
        result.put("completed", toLong(raw.get("completed")));
        result.put("paused", toLong(raw.get("paused")));
        result.put("upcoming", toLong(raw.get("upcoming")));
        return result;
    }

    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }

    @Override
    public Page<PmTask> pageMyTasks(Long employeeId, Long projectId, Integer status, String taskName, Integer pageNum, Integer pageSize) {
        Page<PmTask> page = new Page<>(pageNum, pageSize);

        // ===== 1. 一次查所有"我的根任务"（assignee=me OR stakeholder, parent_id=0/IS NULL） =====
        List<PmTask> allMyRoots = baseMapper.selectAllMyRootTasks(employeeId, projectId, status, taskName);
        if (allMyRoots.isEmpty()) {
            page.setTotal(0L);
            page.setRecords(new ArrayList<>());
            return page;
        }

        // ===== 2. 一次查所有"我的子任务"（assignee=me OR stakeholder, parent_id>0） =====
        List<PmTask> allMySubs = baseMapper.selectAllMySubTasks(employeeId, projectId, status, taskName);

        // ===== 3. 按 parentId 统计每个根的子数 =====
        Map<Long, Integer> subCountMap = new HashMap<>();
        for (PmTask sub : allMySubs) {
            Long parentId = sub.getParentId() != null ? sub.getParentId() : 0L;
            subCountMap.merge(parentId, 1, Integer::sum);
        }

        // ===== 4. 计算每个根的行数 rc[i] = 1 + 子数 =====
        int totalRoots = allMyRoots.size();
        int[] rc = new int[totalRoots];
        for (int i = 0; i < totalRoots; i++) {
            rc[i] = 1 + subCountMap.getOrDefault(allMyRoots.get(i).getId(), 0);
        }
        int totalRows = 0;
        for (int c : rc) totalRows += c;

        // ===== 5. 用"行号累计 + 区间相交"分页（修复 pageOfRoot 误判 bug） =====
        // 关键：旧 pageOfRoot[i] 只记"主要归属页"，大根跨多页时后续页找不到该根 → 空页！
        // 新算法：维护每个根的 [rootStartRow, rootEndRow) 区间，pageNum 用 [pageStart, pageEnd) 区间相交判断。
        int totalPages = (totalRows + pageSize - 1) / pageSize;

        int[] rootStartRow = new int[totalRoots];
        int[] rootEndRow = new int[totalRoots];
        int cumRow = 0;
        for (int i = 0; i < totalRoots; i++) {
            rootStartRow[i] = cumRow;
            cumRow += rc[i];
            rootEndRow[i] = cumRow;
        }

        // ===== 6. 当前页行号范围 =====
        int pageStart = (pageNum - 1) * pageSize;
        int pageEnd = Math.min(pageStart + pageSize, totalRows);

        if (pageStart >= totalRows) {
            page.setTotal((long) totalRows);
            page.setRecords(new ArrayList<>());
            return page;
        }

        // ===== 7. 找出当前页"涉及到的根"（区间相交） =====
        int fromIdx = -1, toIdx = -1;
        for (int i = 0; i < totalRoots; i++) {
            if (rootEndRow[i] > pageStart && rootStartRow[i] < pageEnd) {
                if (fromIdx == -1) fromIdx = i;
                toIdx = i;
            }
            if (rootStartRow[i] >= pageEnd) break;
        }

        // ===== 8. 收集每个根的子任务列表 =====
        Map<Long, List<PmTask>> rootIdToAllSubs = new HashMap<>();
        for (PmTask sub : allMySubs) {
            Long parentId = sub.getParentId() != null ? sub.getParentId() : 0L;
            rootIdToAllSubs.computeIfAbsent(parentId, k -> new ArrayList<>()).add(sub);
        }

        // ===== 9. 拼装当前页 records：根（按需） + 该根在当前页的子切片 =====
        List<PmTask> result = new ArrayList<>();
        for (int i = fromIdx; i <= toIdx; i++) {
            PmTask root = allMyRoots.get(i);
            List<PmTask> allSubs = rootIdToAllSubs.getOrDefault(root.getId(), new ArrayList<>());
            int subCount = allSubs.size();

            // 子切片起止（不含根本身）
            int subStartIdx = Math.max(0, pageStart - rootStartRow[i] - 1);
            int subEndIdx = Math.min(subCount, pageEnd - rootStartRow[i] - 1);
            if (subEndIdx < subStartIdx) subEndIdx = subStartIdx;

            // 根标题是否在当前页（仅 rootStartRow 落在 [pageStart, pageEnd) 时显示）
            boolean showRoot = (pageStart <= rootStartRow[i] && rootStartRow[i] < pageEnd);

            if (showRoot) {
                result.add(root);
            }
            if (subStartIdx < subEndIdx) {
                result.addAll(allSubs.subList(subStartIdx, subEndIdx));
            }
        }

        // ===== 10. 附件处理（保留原逻辑） =====
        result = attachmentService.ensureAttachmentsForTaskList(result);

        // ===== 11. 设置 total = 实际行数（与 stats API 的"任务总数"一致） =====
        page.setTotal((long) totalRows);
        page.setRecords(result);
        return page;
    }

    @Override
    public PmTask getTaskDetail(Long id) {
        PmTask task = baseMapper.selectTaskById(id);
        return attachmentService.ensureAttachmentsHavePreviewUrls(task);
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
        
        // 生成任务编码 - 只有未设置时才生成
        if (task.getTaskCode() == null || task.getTaskCode().isEmpty()) {
            PmProject project = projectMapper.selectById(task.getProjectId());
            String projectCode = project != null && project.getProjectCode() != null ? project.getProjectCode() : "TASK";
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            // 使用纳秒确保单任务创建时也唯一
            String nano = String.valueOf(System.nanoTime() % 1000000);
            task.setTaskCode(projectCode + "-" + timestamp + "-" + nano);
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
        // 如果前端没有传进度，默认为0
        if (task.getProgress() == null) {
            task.setProgress(0);
        }
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
    public List<PmTask> getUpcomingTasks(Long employeeId) {
        return baseMapper.selectUpcomingTasks(employeeId);
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
                List<Long> stakeholderList = mapper.readValue(stakeholders, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
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
    public Page<WorkloadDTO> getWorkloadPage(Integer pageNum, Integer pageSize, Long projectId, String taskName, Long assigneeId, LocalDate taskStartDate, LocalDate taskEndDate, LocalDateTime bindStartTime, LocalDateTime bindEndTime, String orderNo, String title) {
        Page<WorkloadDTO> resultPage = new Page<>(pageNum, pageSize);

        // 1. 先查询总条数
        Long total = baseMapper.countWorkload(projectId, taskName, assigneeId, taskStartDate, taskEndDate, bindStartTime, bindEndTime, orderNo, title);
        resultPage.setTotal(total != null ? total : 0);

        if (total == null || total == 0) {
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }

        // 2. 查询所有符合条件的数据
        List<Map<String, Object>> rawData = baseMapper.selectWorkloadPage(projectId, taskName, assigneeId, taskStartDate, taskEndDate, bindStartTime, bindEndTime, orderNo, title);

        // 3. 在内存中进行分页
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, rawData.size());
        
        List<Map<String, Object>> pageData = fromIndex < rawData.size() 
            ? rawData.subList(fromIndex, toIndex) 
            : new ArrayList<>();

        // 4. 批量收集需要查询的ID
        List<Long> tableIds = new ArrayList<>();
        List<Long> rowIds = new ArrayList<>();
        
        for (Map<String, Object> row : pageData) {
            Object tableId = row.get("table_id");
            Object rowId = row.get("row_id");
            if (tableId != null) {
                tableIds.add(((Number) tableId).longValue());
            }
            if (rowId != null) {
                rowIds.add(((Number) rowId).longValue());
            }
        }

        // 5. 批量查询 customTable 和 customTableRow
        Map<Long, PmCustomTable> tableMap = new HashMap<>();
        Map<Long, PmCustomTableRow> rowMap = new HashMap<>();
        
        if (!tableIds.isEmpty()) {
            List<PmCustomTable> tables = customTableMapper.selectBatchIds(tableIds);
            tableMap = tables.stream().collect(Collectors.toMap(PmCustomTable::getId, t -> t));
        }
        
        if (!rowIds.isEmpty()) {
            List<PmCustomTableRow> rows = customTableRowMapper.selectBatchIds(rowIds);
            rowMap = rows.stream().collect(Collectors.toMap(PmCustomTableRow::getId, r -> r));
        }

        // 6. 构建结果对象
        List<WorkloadDTO> workloadList = new ArrayList<>();
        for (Map<String, Object> row : pageData) {
            WorkloadDTO dto = buildWorkloadDTOFromMap(row, tableMap, rowMap);
            workloadList.add(dto);
        }

        resultPage.setRecords(workloadList);
        return resultPage;
    }

    /**
     * 从Map构建WorkloadDTO
     */
    private WorkloadDTO buildWorkloadDTOFromMap(Map<String, Object> row, Map<Long, PmCustomTable> tableMap, Map<Long, PmCustomTableRow> rowMap) {
        WorkloadDTO dto = new WorkloadDTO();

        // 设置ID
        Object bindingId = row.get("binding_id");
        Object taskId = row.get("task_id");
        if (bindingId != null) {
            dto.setId(((Number) bindingId).longValue());
        } else if (taskId != null) {
            dto.setId(((Number) taskId).longValue());
        }

        // 设置任务相关字段
        if (taskId != null) {
            dto.setTaskId(((Number) taskId).longValue());
        }
        dto.setProjectId(row.get("project_id") != null ? ((Number) row.get("project_id")).longValue() : null);
        dto.setTaskName((String) row.get("task_name"));
        dto.setTaskStatus(row.get("status") != null ? ((Number) row.get("status")).intValue() : null);
        dto.setTaskStatusText(getTaskStatusText(dto.getTaskStatus()));
        
        // 设置日期字段
        if (row.get("start_date") != null) {
            dto.setStartDate(((java.sql.Date) row.get("start_date")).toLocalDate());
        }
        if (row.get("end_date") != null) {
            dto.setEndDate(((java.sql.Date) row.get("end_date")).toLocalDate());
        }
        dto.setProgress(row.get("progress") != null ? ((Number) row.get("progress")).intValue() : null);
        dto.setAssigneeId(row.get("assignee_id") != null ? ((Number) row.get("assignee_id")).longValue() : null);

        // 设置关联名称
        dto.setProjectName((String) row.get("project_name"));
        dto.setAssigneeName((String) row.get("assignee_name"));

        // 设置绑定信息
        Object tableId = row.get("table_id");
        if (tableId != null) {
            dto.setTableId(((Number) tableId).longValue());
            PmCustomTable table = tableMap.get(dto.getTableId());
            if (table != null) {
                dto.setTableName(table.getTableName());
            }
        }
        dto.setOrderNo((String) row.get("order_no"));
        dto.setTitle((String) row.get("title"));
        
        if (row.get("bind_time") != null) {
            dto.setBindTime(((java.sql.Timestamp) row.get("bind_time")).toLocalDateTime());
        }

        // 设置绑定数据
        Object rowId = row.get("row_id");
        if (rowId != null) {
            PmCustomTableRow customRow = rowMap.get(((Number) rowId).longValue());
            if (customRow != null && customRow.getRowData() != null) {
                try {
                    Map<String, Object> rowDataMap = objectMapper.readValue(customRow.getRowData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    dto.setBindData(rowDataMap);
                } catch (Exception e) {
                    log.error("解析行数据失败", e);
                }
            }
        }

        return dto;
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

    /**
     * 将 PmTask 转换为 TaskVO
     */
    private TaskVO convertToVO(PmTask task, Long currentEmployeeId) {
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);
        
        if (currentEmployeeId != null) {
            vo.setHasPermission(hasTaskPermission(task.getId(), currentEmployeeId));
            vo.setHasDeletePermission(hasTaskDeletePermission(task.getId(), currentEmployeeId));
        } else {
            vo.setHasPermission(false);
            vo.setHasDeletePermission(false);
        }
        
        return vo;
    }

    @Override
    public Page<TaskVO> pageTaskList(TaskQueryDTO query, Long currentEmployeeId) {
        Page<PmTask> taskPage = pageList(
            query.getPageNum(),
            query.getPageSize(),
            query.getProjectId(),
            query.getAssigneeId(),
            query.getStatus(),
            query.getPriority(),
            query.getTaskName(),
            query.getAssigneeName()
        );

        Page<TaskVO> voPage = new Page<>(query.getPageNum(), query.getPageSize(), taskPage.getTotal());
        List<TaskVO> voList = taskPage.getRecords().stream()
            .map(task -> convertToVO(task, currentEmployeeId))
            .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public TaskVO getTaskDetailWithPermission(Long id, Long currentEmployeeId) {
        PmTask task = getTaskDetail(id);
        if (task == null) {
            return null;
        }
        return convertToVO(task, currentEmployeeId);
    }

    @Override
    public PmTask createTask(TaskCreateDTO dto) {
        PmTask task = new PmTask();
        BeanUtils.copyProperties(dto, task);
        return createTask(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PmTask> batchCreateTasks(BatchTaskCreateDTO dto) {
        List<PmTask> createdTasks = new ArrayList<>();
        
        if (dto.getTasks().isEmpty()) {
            throw new RuntimeException("任务列表不能为空");
        }
        
        // 从第一个任务中获取项目ID
        Long projectId = dto.getTasks().get(0).getProjectId();
        if (projectId == null) {
            throw new RuntimeException("项目ID不能为空");
        }
        
        PmProject project = projectMapper.selectById(projectId);
        String projectCode = project != null && project.getProjectCode() != null ? project.getProjectCode() : "TASK";
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        int index = 1;
        for (TaskCreateDTO taskDTO : dto.getTasks()) {
            PmTask task = new PmTask();
            BeanUtils.copyProperties(taskDTO, task);
            
            // 确保项目ID一致
            task.setProjectId(projectId);
            
            // 生成唯一的任务编码：项目编码-时间戳-序号
            String taskCode = projectCode + "-" + timestamp + "-" + String.format("%03d", index);
            task.setTaskCode(taskCode);
            
            task = createTask(task);
            createdTasks.add(task);
            index++;
        }
        return createdTasks;
    }

    @Override
    public PmTask updateTask(TaskUpdateDTO dto) {
        PmTask task = new PmTask();
        BeanUtils.copyProperties(dto, task);
        return updateTask(task);
    }
}
