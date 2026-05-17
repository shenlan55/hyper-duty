package com.lasu.hyperduty.pm.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.pm.dto.PmShadowAnnotationVO;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowAnnotationWithProjectVO;
import com.lasu.hyperduty.pm.dto.ShadowTaskCreateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskUpdateDTO;
import com.lasu.hyperduty.pm.dto.ShadowTaskVO;
import com.lasu.hyperduty.pm.entity.PmShadowAnnotation;
import com.lasu.hyperduty.pm.entity.PmTaskShadow;
import com.lasu.hyperduty.pm.service.PmTaskShadowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 影子任务 Controller (v2)
 */
@RestController
@RequestMapping("/pm/shadow")
@RequiredArgsConstructor
public class PmTaskShadowController {

    private final PmTaskShadowService shadowService;

    // ========================================
    // 影子任务 CRUD
    // ========================================

    /**
     * 创建影子任务
     */
    @PostMapping
    public ResponseResult<PmTaskShadow> createShadow(@Valid @RequestBody ShadowTaskCreateDTO dto) {
        String username = SecurityUtil.getCurrentUsername();
        PmTaskShadow shadow = shadowService.createShadow(dto, username);
        return ResponseResult.success(shadow);
    }

    /**
     * 更新影子任务（只更新别名和描述）
     */
    @PutMapping("/{shadowId}")
    public ResponseResult<PmTaskShadow> updateShadow(
            @PathVariable Long shadowId,
            @Valid @RequestBody ShadowTaskUpdateDTO dto) {
        PmTaskShadow shadow = shadowService.updateShadow(shadowId, dto);
        return ResponseResult.success(shadow);
    }

    /**
     * 删除影子任务
     */
    @DeleteMapping("/{shadowId}")
    public ResponseResult<Void> deleteShadow(@PathVariable Long shadowId) {
        shadowService.deleteShadow(shadowId);
        return ResponseResult.success(null);
    }

    // ========================================
    // 查询
    // ========================================

    /**
     * 查询：真实任务 + 影子任务（UNION ALL）
     * @param projectId 项目ID
     * @return 任务列表
     */
    @GetMapping("/project/{projectId}")
    public ResponseResult<List<ShadowTaskVO>> getTaskListWithShadows(@PathVariable Long projectId) {
        Long currentEmployeeId = SecurityUtil.getCurrentUserId();
        List<ShadowTaskVO> tasks = shadowService.getTaskListWithShadows(projectId, currentEmployeeId);
        return ResponseResult.success(tasks);
    }

    /**
     * 查询：影子任务详情
     * @param shadowId 影子ID
     * @return 影子任务 VO
     */
    @GetMapping("/{shadowId}")
    public ResponseResult<ShadowTaskVO> getShadowDetail(@PathVariable Long shadowId) {
        Long currentEmployeeId = SecurityUtil.getCurrentUserId();
        ShadowTaskVO shadow = shadowService.getShadowDetail(shadowId, currentEmployeeId);
        return ResponseResult.success(shadow);
    }

    /**
     * 查询：源任务的所有影子
     * @param sourceTaskId 源任务ID
     * @return 影子列表
     */
    @GetMapping("/source-task/{sourceTaskId}")
    public ResponseResult<List<PmTaskShadow>> getShadowsBySourceTask(@PathVariable Long sourceTaskId) {
        List<PmTaskShadow> shadows = shadowService.getShadowsBySourceTask(sourceTaskId);
        return ResponseResult.success(shadows);
    }

    // ========================================
    // 批注 CRUD
    // ========================================

    /**
     * 添加批注
     */
    @PostMapping("/annotation")
    public ResponseResult<PmShadowAnnotation> addAnnotation(@Valid @RequestBody ShadowAnnotationCreateDTO dto) {
        String username = SecurityUtil.getCurrentUsername();
        PmShadowAnnotation annotation = shadowService.addAnnotation(dto, username);
        return ResponseResult.success(annotation);
    }

    /**
     * 删除批注
     */
    @DeleteMapping("/annotation/{annotationId}")
    public ResponseResult<Void> deleteAnnotation(@PathVariable Long annotationId) {
        shadowService.deleteAnnotation(annotationId);
        return ResponseResult.success(null);
    }

    /**
     * 查询影子的所有批注（包含创建人姓名）
     */
    @GetMapping("/annotation/shadow/{shadowId}")
    public ResponseResult<List<PmShadowAnnotationVO>> getAnnotations(@PathVariable Long shadowId) {
        List<PmShadowAnnotationVO> annotations = shadowService.getAnnotationsByShadowId(shadowId);
        return ResponseResult.success(annotations);
    }

    /**
     * 查询源任务的所有影子批注（包含影子项目信息）
     */
    @GetMapping("/annotation/source-task/{sourceTaskId}")
    public ResponseResult<List<ShadowAnnotationWithProjectVO>> getAnnotationsBySourceTask(@PathVariable Long sourceTaskId) {
        List<ShadowAnnotationWithProjectVO> annotations = shadowService.getAnnotationsBySourceTaskId(sourceTaskId);
        return ResponseResult.success(annotations);
    }
}
