package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysDictData;

import java.util.List;

public interface SysDictDataService extends IService<SysDictData> {

    List<SysDictData> getByDictTypeId(Long dictTypeId);
    
    Page<SysDictData> page(
            Page<SysDictData> page, 
            Long dictTypeId, 
            String keyword);
}