package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.entity.PmTaskProgressUpdate;
import com.lasu.hyperduty.service.PmTaskProgressUpdateService;
import com.lasu.hyperduty.common.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pm/task/progress/update")
@RequiredArgsConstructor
@Slf4j
public class PmTaskProgressUpdateController {

    private final PmTaskProgressUpdateService pmTaskProgressUpdateService;

    /**
     * 创建任务进展更新
     * @param update 任务进展更新信息
     * @return 创建的任务进展更新
     */
    @PostMapping
    public ResponseResult<PmTaskProgressUpdate> createProgressUpdate(@RequestBody PmTaskProgressUpdate update) {
        try {
            PmTaskProgressUpdate created = pmTaskProgressUpdateService.createProgressUpdate(update);
            return ResponseResult.success(created);
        } catch (Exception e) {
            log.error("创建任务进展更新失败", e);
            return ResponseResult.error("创建任务进展更新失败");
        }
    }

    /**
     * 获取任务的所有进展更新，按时间倒序排列
     * @param taskId 任务ID
     * @return 任务进展更新列表
     */
    @GetMapping("/task/{taskId}")
    public ResponseResult<List<PmTaskProgressUpdate>> getTaskProgressUpdates(@PathVariable Long taskId) {
        try {
            List<PmTaskProgressUpdate> updates = pmTaskProgressUpdateService.getTaskProgressUpdates(taskId);
            return ResponseResult.success(updates);
        } catch (Exception e) {
            log.error("获取任务进展更新失败", e);
            return ResponseResult.error("获取任务进展更新失败");
        }
    }

    /**
     * 获取任务进展更新详情
     * @param id 进展更新ID
     * @return 任务进展更新详情
     */
    @GetMapping("/{id}")
    public ResponseResult<PmTaskProgressUpdate> getProgressUpdateDetail(@PathVariable Long id) {
        try {
            PmTaskProgressUpdate update = pmTaskProgressUpdateService.getProgressUpdateDetail(id);
            return ResponseResult.success(update);
        } catch (Exception e) {
            log.error("获取任务进展更新详情失败", e);
            return ResponseResult.error("获取任务进展更新详情失败");
        }
    }
}
