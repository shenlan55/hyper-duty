package com.lasu.hyperduty.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.service.SysDictDataService;
import java.util.List;








public interface SysDictDataService extends IService<SysDictData> {

    List<SysDictData> getByDictTypeId(Long dictTypeId);
    
    Page<SysDictData> page(
            Page<SysDictData> page, 
            Long dictTypeId, 
            String keyword);
}