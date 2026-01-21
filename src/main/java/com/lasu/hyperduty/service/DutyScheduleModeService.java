package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyScheduleMode;
import com.lasu.hyperduty.service.algorithm.ScheduleAlgorithm;

import java.util.List;
import java.util.Map;

/**
 * 排班模式服务接口
 */
public interface DutyScheduleModeService extends IService<DutyScheduleMode> {

    /**
     * 获取所有启用的排班模式
     * @return 启用的排班模式列表
     */
    List<DutyScheduleMode> getEnabledModes();

    /**
     * 根据模式编码获取排班模式
     * @param modeCode 模式编码
     * @return 排班模式
     */
    DutyScheduleMode getByCode(String modeCode);

    /**
     * 获取排班算法实例
     * @param algorithmClass 算法类名
     * @return 排班算法实例
     */
    ScheduleAlgorithm getAlgorithmInstance(String algorithmClass);

    /**
     * 获取排班算法实例
     * @param modeId 排班模式ID
     * @return 排班算法实例
     */
    ScheduleAlgorithm getAlgorithmInstanceByModeId(Long modeId);

    /**
     * 获取模式的配置参数
     * @param modeId 排班模式ID
     * @return 配置参数Map
     */
    Map<String, Object> getModeConfig(Long modeId);

    /**
     * 保存模式的配置参数
     * @param modeId 排班模式ID
     * @param configParams 配置参数Map
     * @return 是否保存成功
     */
    boolean saveModeConfig(Long modeId, Map<String, Object> configParams);
}