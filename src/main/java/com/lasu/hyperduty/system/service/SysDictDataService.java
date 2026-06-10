package com.lasu.hyperduty.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.service.SysDictDataService;
import java.util.List;
import java.util.Map;








public interface SysDictDataService extends IService<SysDictData> {

    List<SysDictData> getByDictTypeId(Long dictTypeId);

    Page<SysDictData> page(
            Page<SysDictData> page,
            Long dictTypeId,
            String keyword);

    /**
     * 按 dict_code 批量查询字典数据
     * 用于前端动态渲染业务枚举（任务状态/班次/审批状态等）
     * @param dictCodes 字典类型编码集合（如 ["task_status","approval_status"]）
     * @return Map<dictCode, List<SysDictData>> 启用状态的字典数据，按 dictSort 升序
     */
    Map<String, List<SysDictData>> getByDictTypeCodes(List<String> dictCodes);
}