package com.lasu.hyperduty.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.system.entity.SysDept;
import com.lasu.hyperduty.system.service.SysDeptService;
import java.util.List;








public interface SysDeptService extends IService<SysDept> {

    List<SysDept> getAllDepts();

    List<SysDept> getDeptTree();

}