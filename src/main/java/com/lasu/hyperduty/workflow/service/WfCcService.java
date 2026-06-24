package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.workflow.dto.WfCcDTO;
import com.lasu.hyperduty.workflow.entity.WfCc;

/**
 * 流程抄送服务
 */
public interface WfCcService extends IService<WfCc> {

    /**
     * 分页查询我的抄送列表
     * @param userId 当前用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param readStatus 阅读状态（null=全部 0=未读 1=已读）
     * @return 抄送分页
     */
    Page<WfCc> pageMine(Long userId, Integer pageNum, Integer pageSize, Integer readStatus);

    /**
     * 新增抄送（任务办理时手动选人 / 节点配置触发）
     */
    WfCc createCc(WfCcDTO dto);

    /**
     * 标记单条已读
     */
    void markRead(Long id, Long userId);

    /**
     * 全部标记已读
     */
    int markAllRead(Long userId);

    /**
     * 节点完成后批量写入抄送（由 Service 内部调用）
     * @param processInstanceId 流程实例ID
     * @param nodeId 当前节点ID
     * @param nodeName 当前节点名称
     * @param userIds 抄送人ID列表
     * @param fromUserId 发起人
     * @param fromUserName 发起人姓名
     */
    void batchCreateForNode(String processInstanceId, String processDefinitionId, String processName,
                            String nodeId, String nodeName,
                            java.util.List<Long> userIds,
                            Long fromUserId, String fromUserName,
                            String title, String content);
}
