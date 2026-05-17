package com.lasu.hyperduty.pm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.pm.dto.PmShadowAnnotationVO;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationWithProjectVO;
import com.lasu.hyperduty.pm.dto.ShadowTaskCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskUpdateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskVO;
import com.lasu.hyperduty.pm.entity.PmShadowAnnotation;
import com.lasu.hyperduty.pm.entity.PmTaskShadow;
import java.util.List;

/**
 * 影子任务 Service 接口 (v2)
 */
public interface PmTaskShadowService {

    // ========================================
    // 影子任务 CRUD
    // ========================================

    /**
     * 创建影子任务
     */
    PmTaskShadow createShadow(ShadowTaskCreateDTO dto, String username);

    /**
     * 更新影子任务（只更新别名和描述）
     */
    PmTaskShadow updateShadow(Long shadowId, ShadowTaskUpdateDTO dto);

    /**
     * 删除影子任务
     */
    void deleteShadow(Long shadowId);

    // ========================================
    // 查询
    // ========================================

    /**
     * 查询：真实任务 + 影子任务（UNION ALL）- 分页
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param projectId 项目ID
     * @param taskName 任务名称（模糊查询）
     * @param assigneeName 负责人姓名（模糊查询）
     * @param status 状态
     * @param priority 优先级
     * @param currentEmployeeId 当前用户ID
     * @return 分页结果
     */
    Page<ShadowTaskVO> pageTaskListWithShadows(Integer pageNum, Integer pageSize, Long projectId, String taskName, String assigneeName, Integer status, Integer priority, Long currentEmployeeId);

    /**
     * 查询：真实任务 + 影子任务（UNION ALL）
     * @param projectId 项目ID
     * @param currentEmployeeId 当前用户ID
     * @return 任务列表
     */
    List<ShadowTaskVO> getTaskListWithShadows(Long projectId, Long currentEmployeeId);

    /**
     * 查询：影子任务详情
     * @param shadowId 影子ID
     * @param currentEmployeeId 当前用户ID
     * @return 影子任务 VO
     */
    ShadowTaskVO getShadowDetail(Long shadowId, Long currentEmployeeId);

    /**
     * 查询：源任务的所有影子
     * @param sourceTaskId 源任务ID
     * @return 影子列表
     */
    List<PmTaskShadow> getShadowsBySourceTask(Long sourceTaskId);

    // ========================================
    // 批注 CRUD
    // ========================================

    /**
     * 添加批注
     */
    PmShadowAnnotation addAnnotation(ShadowAnnotationCreateDTO dto, String username);

    /**
     * 删除批注
     */
    void deleteAnnotation(Long annotationId);

    /**
     * 查询影子的所有批注（包含创建人姓名）
     */
    List<PmShadowAnnotationVO> getAnnotationsByShadowId(Long shadowId);

    /**
     * 查询源任务的所有影子批注（包含影子项目信息）
     */
    List<ShadowAnnotationWithProjectVO> getAnnotationsBySourceTaskId(Long sourceTaskId);
}
