package com.lasu.hyperduty.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lasu.hyperduty.workflow.dto.WfUrgeDTO;
import com.lasu.hyperduty.workflow.entity.WfUrgeRecord;

public interface WfUrgeService {

    /**
     * 发起催办
     * @param dto 催办参数
     */
    void urge(WfUrgeDTO dto);

    /**
     * 分页：我发起的催办
     * @param page 分页
     * @return 催办记录分页
     */
    IPage<WfUrgeRecord> pageSent(IPage<WfUrgeRecord> page);

    /**
     * 分页：我接收的催办
     * @param page 分页
     * @return 催办记录分页
     */
    IPage<WfUrgeRecord> pageReceived(IPage<WfUrgeRecord> page);
}
