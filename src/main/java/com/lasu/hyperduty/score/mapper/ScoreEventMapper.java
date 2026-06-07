package com.lasu.hyperduty.score.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.score.entity.ScoreEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分事件 Mapper
 */
@Mapper
public interface ScoreEventMapper extends BaseMapper<ScoreEvent> {
}