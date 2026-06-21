package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.dto.ShadowTaskVO;
import com.lasu.hyperduty.pm.entity.PmTaskShadow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 影子任务 Mapper (v2)
 */
@Mapper
public interface PmTaskShadowMapper extends BaseMapper<PmTaskShadow> {

    /**
     * 查询：真实任务 + 影子任务（UNION ALL）根任务分页
     * 业务过滤下沉到 SQL + LIMIT/OFFSET，关联子查询改 LATERAL
     * @param projectId 项目ID
     * @param taskName 任务名（模糊）
     * @param assigneeName 负责人名（模糊）
     * @param status 状态
     * @param priority 优先级
     * @param assigneeId 负责人ID
     * @param offset 偏移
     * @param pageSize 每页条数
     * @return 当前页根任务列表
     */
    List<ShadowTaskVO> selectRootTaskPageWithShadows(
            @Param("projectId") Long projectId,
            @Param("taskName") String taskName,
            @Param("assigneeName") String assigneeName,
            @Param("status") Integer status,
            @Param("priority") Integer priority,
            @Param("assigneeId") Long assigneeId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    /**
     * 查询：根任务总数（真实任务 + 影子任务，分别计数求和）
     */
    long countRootTaskWithShadows(
            @Param("projectId") Long projectId,
            @Param("taskName") String taskName,
            @Param("assigneeName") String assigneeName,
            @Param("status") Integer status,
            @Param("priority") Integer priority,
            @Param("assigneeId") Long assigneeId);

    /**
     * 查询：子树（按 parent_id IN 批量拉取真实子任务 + 影子子任务）
     * 用于"根任务+完整子树一页"的跟随展示
     */
    List<ShadowTaskVO> selectSubTasksByRootIds(
            @Param("rootIds") List<Long> rootIds,
            @Param("projectId") Long projectId);

    /**
     * 查询：项目下所有根任务（真实任务 + 影子任务 UNION ALL，无分页/过滤）
     * 用途：旧 API getTaskListWithShadows / 导出场景
     */
    List<ShadowTaskVO> selectAllRootTasksWithShadows(@Param("projectId") Long projectId);

    /**
     * 查询：项目下所有非根任务（真实子 + 影子子，无 IN 限制）
     * 用途：按"行"分页时一次性拉所有子，service 层做内存切分（根任务不可切断）
     */
    List<ShadowTaskVO> selectAllSubTasksWithShadows(@Param("projectId") Long projectId);

    /**
     * 查询：影子任务详情
     * @param shadowId 影子ID
     * @return 影子任务 VO
     */
    ShadowTaskVO selectShadowById(@Param("shadowId") Long shadowId);

    /**
     * 查询：源任务的所有影子
     * @param sourceTaskId 源任务ID
     * @return 影子列表
     */
    List<PmTaskShadow> selectShadowsBySourceTaskId(@Param("sourceTaskId") Long sourceTaskId);

    /**
     * 查询指定项目列表的所有影子任务
     */
    @Select("<script>" +
            "SELECT s.*, " +
            "t.task_name as source_task_name, t.progress as source_progress, t.status as source_status, " +
            "t.priority as source_priority, t.description as source_description, " +
            "t.start_date as source_start_date, t.end_date as source_end_date, " +
            "e.employee_name as source_owner_name, t.attachments as source_attachments, " +
            "p1.project_name as source_project_name, p2.project_name as target_project_name, " +
            "ce.employee_name as created_by_name, " +
            "(SELECT MAX(create_time) FROM pm_task_progress_update WHERE task_id = s.source_task_id) as last_progress_update_time " +
            "FROM pm_task_shadow s " +
            "LEFT JOIN pm_task t ON s.source_task_id = t.id " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p1 ON t.project_id = p1.id " +
            "LEFT JOIN pm_project p2 ON s.project_id = p2.id " +
            "LEFT JOIN sys_employee ce ON s.created_by = ce.username " +
            "<where>" +
            "<if test='projectIds != null and projectIds.size() > 0'> AND s.project_id IN " +
            "<foreach collection='projectIds' item='projectId' open='(' separator=',' close=')'>#{projectId}</foreach>" +
            "</if>" +
            "<if test='taskStartDateFrom != null'> AND t.start_date &gt;= #{taskStartDateFrom}</if>" +
            "<if test='taskStartDateTo != null'> AND t.start_date &lt;= #{taskStartDateTo}</if>" +
            "<if test='taskEndDateFrom != null'> AND t.end_date &gt;= #{taskEndDateFrom}</if>" +
            "<if test='taskEndDateTo != null'> AND t.end_date &lt;= #{taskEndDateTo}</if>" +
            "</where>" +
            "ORDER BY p2.project_name, s.created_at DESC" +
            "</script>")
    List<PmTaskShadow> selectShadowTasksForReport(
            @Param("projectIds") List<Long> projectIds,
            @Param("taskStartDateFrom") java.time.LocalDate taskStartDateFrom,
            @Param("taskStartDateTo") java.time.LocalDate taskStartDateTo,
            @Param("taskEndDateFrom") java.time.LocalDate taskEndDateFrom,
            @Param("taskEndDateTo") java.time.LocalDate taskEndDateTo);
}
