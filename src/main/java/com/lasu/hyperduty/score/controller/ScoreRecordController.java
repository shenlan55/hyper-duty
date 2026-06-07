package com.lasu.hyperduty.score.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.PageResult;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.score.entity.ScoreEvent;
import com.lasu.hyperduty.score.entity.ScoreRecord;
import com.lasu.hyperduty.score.service.ScoreEventService;
import com.lasu.hyperduty.score.service.ScoreRecordService;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分记录 Controller
 */
@RestController
@RequestMapping("/score/records")
public class ScoreRecordController {

    @Autowired
    private ScoreRecordService scoreRecordService;

    @Autowired
    private ScoreEventService scoreEventService;

    @Autowired
    private SysEmployeeMapper sysEmployeeMapper;

    /**
     * 分页查询积分记录
     */
    @GetMapping
    public ResponseResult<PageResult<ScoreRecord>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer periodYear,
            @RequestParam(required = false) Integer periodMonth) {

        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) {
            wrapper.eq(ScoreRecord::getEmployeeId, employeeId);
        }
        if (periodYear != null) {
            wrapper.eq(ScoreRecord::getPeriodYear, periodYear);
        }
        if (periodMonth != null) {
            wrapper.eq(ScoreRecord::getPeriodMonth, periodMonth);
        }
        wrapper.orderByDesc(ScoreRecord::getCreateTime);

        Page<ScoreRecord> pageResult = scoreRecordService.page(new Page<>(page, pageSize), wrapper);

        // 填充员工姓名和事件名称
        for (ScoreRecord record : pageResult.getRecords()) {
            SysEmployee employee = sysEmployeeMapper.selectById(record.getEmployeeId());
            if (employee != null) {
                record.setEmployeeName(employee.getEmployeeName());
            }
            ScoreEvent event = scoreEventService.getById(record.getEventId());
            if (event != null) {
                record.setEventName(event.getEventName());
            }
        }

        PageResult<ScoreRecord> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setData(pageResult.getRecords());
        return ResponseResult.success(result);
    }

    /**
     * 录入积分记录
     */
    @PostMapping
    public ResponseResult<String> create(@RequestBody ScoreRecord record) {
        // 根据 recordDate 自动设置月份
        if (record.getRecordDate() != null) {
            record.setPeriodYear(record.getRecordDate().getYear());
            record.setPeriodMonth(record.getRecordDate().getMonthValue());
        }
        record.setCreateTime(LocalDateTime.now());
        scoreRecordService.save(record);
        return ResponseResult.success("录入成功");
    }

    /**
     * 删除积分记录
     */
    @DeleteMapping("/{id}")
    public ResponseResult<String> delete(@PathVariable Long id) {
        scoreRecordService.removeById(id);
        return ResponseResult.success("删除成功");
    }
}