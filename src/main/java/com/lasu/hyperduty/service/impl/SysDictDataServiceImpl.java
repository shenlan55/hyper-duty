package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SysDictData;
import com.lasu.hyperduty.mapper.SysDictDataMapper;
import com.lasu.hyperduty.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @Override
    public List<SysDictData> getByDictTypeId(Long dictTypeId) {
        return sysDictDataMapper.selectByDictTypeId(dictTypeId);
    }
}