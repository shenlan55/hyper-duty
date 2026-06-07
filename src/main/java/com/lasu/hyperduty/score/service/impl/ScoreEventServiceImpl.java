package com.lasu.hyperduty.score.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.score.entity.ScoreEvent;
import com.lasu.hyperduty.score.mapper.ScoreEventMapper;
import com.lasu.hyperduty.score.service.ScoreEventService;
import org.springframework.stereotype.Service;

/**
 * 积分事件 Service 实现
 */
@Service
public class ScoreEventServiceImpl extends ServiceImpl<ScoreEventMapper, ScoreEvent> implements ScoreEventService {
}