package com.lasu.hyperduty.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.pm.entity.PmTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;








@Mapper
public interface PmTaskMapper extends BaseMapper<PmTask> {

    /**
     * 根任务分页：真实任务版（按"根任务+完整子树一页"分页）
     * 关联子查询改 LATERAL + 走 task_id+create_time 索引
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "lpu.last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN LATERAL ( " +
            "    SELECT MAX(create_time) AS last_progress_update_time " +
            "    FROM pm_task_progress_update " +
            "    WHERE task_id = t.id " +
            ") lpu ON TRUE " +
            "<where> " +
            "  (t.parent_id IS NULL OR t.parent_id = 0) " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "<if test='assigneeId != null'> AND t.assignee_id = #{assigneeId}</if> " +
            "<if test='status != null'> AND t.status = #{status}</if> " +
            "<if test='priority != null'> AND t.priority = #{priority}</if> " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "<if test='assigneeName != null and assigneeName != \"\"'> AND e.employee_name ILIKE CONCAT('%', #{assigneeName}, '%')</if> " +
            "</where> " +
            "ORDER BY t.is_pinned DESC, " +
            "CASE WHEN t.status IN (1, 2) THEN 0 " +
            "     WHEN t.status = 3 THEN 1 " +
            "     WHEN t.status = 4 THEN 2 " +
            "     ELSE 3 END, " +
            "t.priority ASC, t.create_time DESC " +
            "LIMIT #{pageSize} OFFSET #{offset} " +
            "</script>")
    List<PmTask> selectRootTaskPage(@Param("projectId") Long projectId,
                                    @Param("assigneeId") Long assigneeId,
                                    @Param("status") Integer status,
                                    @Param("priority") Integer priority,
                                    @Param("taskName") String taskName,
                                    @Param("assigneeName") String assigneeName,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    /**
     * 根任务总数：真实任务版
     */
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "<where> " +
            "  (t.parent_id IS NULL OR t.parent_id = 0) " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "<if test='assigneeId != null'> AND t.assignee_id = #{assigneeId}</if> " +
            "<if test='status != null'> AND t.status = #{status}</if> " +
            "<if test='priority != null'> AND t.priority = #{priority}</if> " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "<if test='assigneeName != null and assigneeName != \"\"'> AND e.employee_name ILIKE CONCAT('%', #{assigneeName}, '%')</if> " +
            "</where> " +
            "</script>")
    Long countRootTask(@Param("projectId") Long projectId,
                       @Param("assigneeId") Long assigneeId,
                       @Param("status") Integer status,
                       @Param("priority") Integer priority,
                       @Param("taskName") String taskName,
                       @Param("assigneeName") String assigneeName);

    /**
     * 子树批量：按 parent_id IN (rootIds) 拉所有子任务
     * 用于"根任务+完整子树一页"展示
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "lpu.last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN LATERAL ( " +
            "    SELECT MAX(create_time) AS last_progress_update_time " +
            "    FROM pm_task_progress_update " +
            "    WHERE task_id = t.id " +
            ") lpu ON TRUE " +
            "WHERE t.parent_id IN " +
            "<foreach collection='rootIds' item='rid' open='(' separator=',' close=')'>#{rid}</foreach> " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "ORDER BY t.create_time DESC " +
            "</script>")
    List<PmTask> selectSubTasksByRootIds(@Param("rootIds") List<Long> rootIds,
                                          @Param("projectId") Long projectId);

    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM pm_task t " +
            "<where>" +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if>" +
            "<if test='assigneeId != null'> AND t.assignee_id = #{assigneeId}</if>" +
            "<if test='status != null'> AND t.status = #{status}</if>" +
            "<if test='priority != null'> AND t.priority = #{priority}</if>" +
            "</where>" +
            "</script>")
    Long selectTaskCount(@Param("projectId") Long projectId,
                         @Param("assigneeId") Long assigneeId,
                         @Param("status") Integer status,
                         @Param("priority") Integer priority);

    @Select("SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "(SELECT MAX(create_time) FROM pm_task_progress_update WHERE task_id = t.id) as last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "WHERE t.project_id = #{projectId} " +
            "ORDER BY t.is_pinned DESC, " +
            "t.task_level ASC, " +
            "CASE WHEN t.status IN (1, 2) THEN 0 " +
            "     WHEN t.status = 3 THEN 1 " +
            "     WHEN t.status = 4 THEN 2 " +
            "     ELSE 3 END, " +
            "t.priority ASC")
    List<PmTask> selectByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT t.*, e.employee_name as owner_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "WHERE t.parent_id = #{parentId} " +
            "ORDER BY t.priority ASC, t.end_date ASC")
    List<PmTask> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 我的任务：用 EXISTS 替代 stakeholders LIKE，加 LATERAL 走索引
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id) as sub_task_count, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id AND sub.status = 3) as completed_sub_task_count, " +
            "lpu.last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN LATERAL ( " +
            "    SELECT MAX(create_time) AS last_progress_update_time " +
            "    FROM pm_task_progress_update " +
            "    WHERE task_id = t.id " +
            ") lpu ON TRUE " +
            "WHERE (t.assignee_id = #{employeeId} " +
            "    OR EXISTS (SELECT 1 FROM pm_task_stakeholder s WHERE s.task_id = t.id AND s.employee_id = #{employeeId})) " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "ORDER BY t.is_pinned DESC, " +
            "CASE WHEN t.status IN (1, 2) THEN 0 " +
            "     WHEN t.status = 3 THEN 1 " +
            "     WHEN t.status = 4 THEN 2 " +
            "     ELSE 3 END, " +
            "t.priority ASC, t.create_time DESC " +
            "</script>")
    List<PmTask> selectMyTasks(@Param("employeeId") Long employeeId, @Param("taskName") String taskName);

    /**
     * 我的任务（按项目）：用 EXISTS 替代 stakeholders LIKE
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id) as sub_task_count, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id AND sub.status = 3) as completed_sub_task_count, " +
            "lpu.last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN LATERAL ( " +
            "    SELECT MAX(create_time) AS last_progress_update_time " +
            "    FROM pm_task_progress_update " +
            "WHERE task_id = t.id " +
            ") lpu ON TRUE " +
            "WHERE t.project_id = #{projectId} " +
            "  AND (t.assignee_id = #{employeeId} " +
            "    OR EXISTS (SELECT 1 FROM pm_task_stakeholder s WHERE s.task_id = t.id AND s.employee_id = #{employeeId})) " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "ORDER BY t.is_pinned DESC, " +
            "CASE WHEN t.status IN (1, 2) THEN 0 " +
            "     WHEN t.status = 3 THEN 1 " +
            "     WHEN t.status = 4 THEN 2 " +
            "     ELSE 3 END, " +
            "t.priority ASC, t.create_time DESC " +
            "</script>")
    List<PmTask> selectMyTasksByProject(@Param("employeeId") Long employeeId, @Param("projectId") Long projectId, @Param("taskName") String taskName);

    /**
     * 我的任务分页 - 真实任务（SQL 真分页，含 status 过滤）
     * 替代 selectMyTasks 全量拉取导致的卡顿
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id) as sub_task_count, " +
            "(SELECT COUNT(*) FROM pm_task sub WHERE sub.parent_id = t.id AND sub.status = 3) as completed_sub_task_count, " +
            "lpu.last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN LATERAL ( " +
            "    SELECT MAX(create_time) AS last_progress_update_time " +
            "    FROM pm_task_progress_update " +
            "    WHERE task_id = t.id " +
            ") lpu ON TRUE " +
            "WHERE (t.assignee_id = #{employeeId} " +
            "    OR EXISTS (SELECT 1 FROM pm_task_stakeholder s WHERE s.task_id = t.id AND s.employee_id = #{employeeId})) " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "<if test='status != null'> AND t.status = #{status}</if> " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "ORDER BY t.is_pinned DESC, " +
            "CASE WHEN t.status IN (1, 2) THEN 0 " +
            "     WHEN t.status = 3 THEN 1 " +
            "     WHEN t.status = 4 THEN 2 " +
            "     ELSE 3 END, " +
            "t.priority ASC, t.create_time DESC " +
            "LIMIT #{pageSize} OFFSET #{offset} " +
            "</script>")
    List<PmTask> pageMyTasks(@Param("employeeId") Long employeeId,
                              @Param("projectId") Long projectId,
                              @Param("status") Integer status,
                              @Param("taskName") String taskName,
                              @Param("offset") Integer offset,
                              @Param("pageSize") Integer pageSize);

    /**
     * 我的任务计数（与 pageMyTasks WHERE 完全一致）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM pm_task t " +
            "WHERE (t.assignee_id = #{employeeId} " +
            "    OR EXISTS (SELECT 1 FROM pm_task_stakeholder s WHERE s.task_id = t.id AND s.employee_id = #{employeeId})) " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "<if test='status != null'> AND t.status = #{status}</if> " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "</script>")
    long countMyTasks(@Param("employeeId") Long employeeId,
                      @Param("projectId") Long projectId,
                      @Param("status") Integer status,
                      @Param("taskName") String taskName);

    /**
     * 我的任务 - 一次查所有根任务（assignee=me OR stakeholder, parent_id=0/IS NULL）
     * 用于："根不可切断"分页算法（避免 SQL 切断根与子）
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "0 as sub_task_count, 0 as completed_sub_task_count, " +
            "lpu.last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN LATERAL ( " +
            "    SELECT MAX(create_time) AS last_progress_update_time " +
            "    FROM pm_task_progress_update " +
            "    WHERE task_id = t.id " +
            ") lpu ON TRUE " +
            "WHERE (t.assignee_id = #{employeeId} " +
            "    OR EXISTS (SELECT 1 FROM pm_task_stakeholder s WHERE s.task_id = t.id AND s.employee_id = #{employeeId})) " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "<if test='status != null'> AND t.status = #{status}</if> " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "AND (t.parent_id IS NULL OR t.parent_id = 0) " +
            "ORDER BY t.is_pinned DESC, " +
            "CASE WHEN t.status IN (1, 2) THEN 0 " +
            "     WHEN t.status = 3 THEN 1 " +
            "     WHEN t.status = 4 THEN 2 " +
            "     ELSE 3 END, " +
            "t.priority ASC, t.create_time DESC " +
            "</script>")
    List<PmTask> selectAllMyRootTasks(@Param("employeeId") Long employeeId,
                                       @Param("projectId") Long projectId,
                                       @Param("status") Integer status,
                                       @Param("taskName") String taskName);

    /**
     * 我的任务 - 一次查所有子任务（assignee=me OR stakeholder, parent_id>0）
     * 用于："根不可切断"分页算法（service 层内存切分）
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "0 as sub_task_count, 0 as completed_sub_task_count, " +
            "lpu.last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN LATERAL ( " +
            "    SELECT MAX(create_time) AS last_progress_update_time " +
            "    FROM pm_task_progress_update " +
            "    WHERE task_id = t.id " +
            ") lpu ON TRUE " +
            "WHERE (t.assignee_id = #{employeeId} " +
            "    OR EXISTS (SELECT 1 FROM pm_task_stakeholder s WHERE s.task_id = t.id AND s.employee_id = #{employeeId})) " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "<if test='status != null'> AND t.status = #{status}</if> " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "AND t.parent_id IS NOT NULL AND t.parent_id &lt;&gt; 0 " +
            "ORDER BY t.create_time DESC " +
            "</script>")
    List<PmTask> selectAllMySubTasks(@Param("employeeId") Long employeeId,
                                     @Param("projectId") Long projectId,
                                     @Param("status") Integer status,
                                     @Param("taskName") String taskName);

    /**
     * 我的任务统计（按状态计数）
     * 一条 SQL 用 FILTER 聚合 4 个状态
     */
    @Select("<script>" +
            "SELECT " +
            "  COUNT(*) AS total, " +
            "  COUNT(*) FILTER (WHERE t.status = 1) AS pending, " +
            "  COUNT(*) FILTER (WHERE t.status = 2) AS in_progress, " +
            "  COUNT(*) FILTER (WHERE t.status = 3) AS completed, " +
            "  COUNT(*) FILTER (WHERE t.status = 4) AS paused, " +
            "  COUNT(*) FILTER (WHERE t.end_date >= CURRENT_DATE AND t.end_date &lt;= CURRENT_DATE + INTERVAL '7 days' AND t.status IN (1, 2)) AS upcoming " +
            "FROM pm_task t " +
            "WHERE (t.assignee_id = #{employeeId} " +
            "    OR EXISTS (SELECT 1 FROM pm_task_stakeholder s WHERE s.task_id = t.id AND s.employee_id = #{employeeId})) " +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if> " +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name ILIKE CONCAT('%', #{taskName}, '%')</if> " +
            "</script>")
    Map<String, Object> selectMyTaskStats(@Param("employeeId") Long employeeId,
                                          @Param("projectId") Long projectId,
                                          @Param("taskName") String taskName);

    @Select("SELECT t.*, e.employee_name as owner_name, p.project_name, pt.task_name as parentTaskName " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN pm_task pt ON t.parent_id = pt.id " +
            "WHERE t.id = #{id}")
    PmTask selectTaskById(@Param("id") Long id);

    @Select("SELECT t.*, e.employee_name as owner_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "WHERE t.status = #{status} " +
            "ORDER BY t.is_pinned DESC, " +
            "CASE WHEN t.status IN (1, 2) THEN 0 " +
            "     WHEN t.status = 3 THEN 1 " +
            "     WHEN t.status = 4 THEN 2 " +
            "     ELSE 3 END, " +
            "t.priority ASC")
    List<PmTask> selectByStatus(@Param("status") Integer status);

    @Select("SELECT t.*, e.employee_name as owner_name, p.project_name " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "WHERE t.end_date <= CURRENT_DATE + INTERVAL '3 days' AND t.status != 3 " +
            "AND (t.assignee_id = #{employeeId} OR t.stakeholders LIKE CONCAT('%', #{employeeId}, '%')) " +
            "ORDER BY t.end_date ASC, t.priority ASC")
    List<PmTask> selectUpcomingTasks(@Param("employeeId") Long employeeId);

    /**
     * 查询指定项目列表的所有源任务
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "(SELECT MAX(create_time) FROM pm_task_progress_update WHERE task_id = t.id) as last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "<where>" +
            "<if test='projectIds != null and projectIds.size() > 0'> AND t.project_id IN " +
            "<foreach collection='projectIds' item='projectId' open='(' separator=',' close=')'>#{projectId}</foreach>" +
            "</if>" +
            "<if test='taskStartDateFrom != null'> AND t.start_date &gt;= #{taskStartDateFrom}</if>" +
            "<if test='taskStartDateTo != null'> AND t.start_date &lt;= #{taskStartDateTo}</if>" +
            "<if test='taskEndDateFrom != null'> AND t.end_date &gt;= #{taskEndDateFrom}</if>" +
            "<if test='taskEndDateTo != null'> AND t.end_date &lt;= #{taskEndDateTo}</if>" +
            "</where>" +
            "ORDER BY p.project_name, t.create_time DESC" +
            "</script>")
    List<PmTask> selectTasksForReport(
            @Param("projectIds") List<Long> projectIds,
            @Param("taskStartDateFrom") java.time.LocalDate taskStartDateFrom,
            @Param("taskStartDateTo") java.time.LocalDate taskStartDateTo,
            @Param("taskEndDateFrom") java.time.LocalDate taskEndDateFrom,
            @Param("taskEndDateTo") java.time.LocalDate taskEndDateTo);

    /**
     * 根据任务ID列表查询任务
     */
    @Select("<script>" +
            "SELECT t.*, e.employee_name as owner_name, p.project_name, " +
            "(SELECT MAX(create_time) FROM pm_task_progress_update WHERE task_id = t.id) as last_progress_update_time " +
            "FROM pm_task t " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "WHERE t.id IN " +
            "<foreach collection='taskIds' item='taskId' open='(' separator=',' close=')'>#{taskId}</foreach>" +
            "ORDER BY p.project_name, t.create_time DESC" +
            "</script>")
    List<PmTask> selectTasksByIds(@Param("taskIds") List<Long> taskIds);

    /**
     * 分页查询工作量数据
     */
    @Select("<script>" +
            "SELECT DISTINCT t.id as task_id, t.*, p.project_name, e.employee_name as assignee_name, " +
            "b.id as binding_id, b.table_id, b.row_id, b.order_no, b.title, b.create_time as bind_time " +
            "FROM pm_task t " +
            "LEFT JOIN pm_project p ON t.project_id = p.id " +
            "LEFT JOIN sys_employee e ON t.assignee_id = e.id " +
            "LEFT JOIN pm_task_custom_row b ON t.id = b.task_id " +
            "<where>" +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if>" +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name LIKE CONCAT('%', #{taskName}, '%')</if>" +
            "<if test='assigneeId != null'> AND t.assignee_id = #{assigneeId}</if>" +
            "<if test='taskStartDate != null'> AND t.start_date &gt;= #{taskStartDate}</if>" +
            "<if test='taskEndDate != null'> AND t.end_date &lt;= #{taskEndDate}</if>" +
            "<if test='bindStartTime != null'> AND b.create_time &gt;= #{bindStartTime}</if>" +
            "<if test='bindEndTime != null'> AND b.create_time &lt;= #{bindEndTime}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND b.order_no LIKE CONCAT('%', #{orderNo}, '%')</if>" +
            "<if test='title != null and title != \"\"'> AND b.title LIKE CONCAT('%', #{title}, '%')</if>" +
            "</where>" +
            "ORDER BY t.create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectWorkloadPage(
            @Param("projectId") Long projectId,
            @Param("taskName") String taskName,
            @Param("assigneeId") Long assigneeId,
            @Param("taskStartDate") java.time.LocalDate taskStartDate,
            @Param("taskEndDate") java.time.LocalDate taskEndDate,
            @Param("bindStartTime") java.time.LocalDateTime bindStartTime,
            @Param("bindEndTime") java.time.LocalDateTime bindEndTime,
            @Param("orderNo") String orderNo,
            @Param("title") String title);

    /**
     * 计算工作量查询总条数
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT CASE WHEN b.id IS NOT NULL THEN CONCAT(t.id::TEXT, '_', b.id::TEXT) ELSE t.id::TEXT END) " +
            "FROM pm_task t " +
            "LEFT JOIN pm_task_custom_row b ON t.id = b.task_id " +
            "<where>" +
            "<if test='projectId != null'> AND t.project_id = #{projectId}</if>" +
            "<if test='taskName != null and taskName != \"\"'> AND t.task_name LIKE CONCAT('%', #{taskName}, '%')</if>" +
            "<if test='assigneeId != null'> AND t.assignee_id = #{assigneeId}</if>" +
            "<if test='taskStartDate != null'> AND t.start_date &gt;= #{taskStartDate}</if>" +
            "<if test='taskEndDate != null'> AND t.end_date &lt;= #{taskEndDate}</if>" +
            "<if test='bindStartTime != null'> AND b.create_time &gt;= #{bindStartTime}</if>" +
            "<if test='bindEndTime != null'> AND b.create_time &lt;= #{bindEndTime}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND b.order_no LIKE CONCAT('%', #{orderNo}, '%')</if>" +
            "<if test='title != null and title != \"\"'> AND b.title LIKE CONCAT('%', #{title}, '%')</if>" +
            "</where>" +
            "</script>")
    Long countWorkload(
            @Param("projectId") Long projectId,
            @Param("taskName") String taskName,
            @Param("assigneeId") Long assigneeId,
            @Param("taskStartDate") java.time.LocalDate taskStartDate,
            @Param("taskEndDate") java.time.LocalDate taskEndDate,
            @Param("bindStartTime") java.time.LocalDateTime bindStartTime,
            @Param("bindEndTime") java.time.LocalDateTime bindEndTime,
            @Param("orderNo") String orderNo,
            @Param("title") String title);
}
