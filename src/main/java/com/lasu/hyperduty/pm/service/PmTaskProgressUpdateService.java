package com.lasu.hyperduty.pm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.pm.entity.PmTaskProgressUpdate;
import com.lasu.hyperduty.pm.service.PmTaskProgressUpdateService;
import java.util.List;








public interface PmTaskProgressUpdateService extends IService<PmTaskProgressUpdate> {

    /**
     * 创建任务进展更新
     * @param update 任务进展更新信息
     * @return 创建的任务进展更新
     */
    PmTaskProgressUpdate createProgressUpdate(PmTaskProgressUpdate update);

    /**
     * 获取任务的所有进展更新，按时间倒序排列
     * @param taskId 任务ID
     * @return 任务进展更新列表
     */
    List<PmTaskProgressUpdate> getTaskProgressUpdates(Long taskId);

    /**
     * 获取任务进展更新详情
     * @param id 进展更新ID
     * @return 任务进展更新详情
     */
    PmTaskProgressUpdate getProgressUpdateDetail(Long id);
}
