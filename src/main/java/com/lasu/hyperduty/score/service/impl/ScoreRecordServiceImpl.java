package com.lasu.hyperduty.score.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.score.entity.ScoreRecord;
import com.lasu.hyperduty.score.mapper.ScoreRecordMapper;
import com.lasu.hyperduty.score.service.ScoreRecordService;
import org.springframework.stereotype.Service;

/**
 * 积分记录 Service 实现
 */
@Service
public class ScoreRecordServiceImpl extends ServiceImpl<ScoreRecordMapper, ScoreRecord> implements ScoreRecordService {
}