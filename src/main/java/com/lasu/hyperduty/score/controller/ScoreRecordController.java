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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

        // 2026-06-27 修复：未指定 employeeId 时，过滤掉禁用员工的记录
        // 指定了 employeeId 时不做过滤（用户明确要看某人的记录，可能用于历史查询/审计）
        if (employeeId == null) {
            Set<Long> activeEmployeeIds = sysEmployeeMapper.selectList(
                            new LambdaQueryWrapper<SysEmployee>()
                                    .eq(SysEmployee::getStatus, 1)
                                    .select(SysEmployee::getId)
                    )
                    .stream()
                    .map(SysEmployee::getId)
                    .collect(Collectors.toSet());
            if (activeEmployeeIds.isEmpty()) {
                // 无启用员工，直接返回空分页
                PageResult<ScoreRecord> empty = new PageResult<>();
                empty.setTotal(0);
                empty.setData(java.util.Collections.emptyList());
                return ResponseResult.success(empty);
            }
            // 构造分页：只查启用员工的记录
            // 由于 wrapper.in() 需 List，先转成 List（保留 Set 性能优势也可，但需要 .in(List) 重载）
            List<Long> activeIdList = new java.util.ArrayList<>(activeEmployeeIds);
            Page<ScoreRecord> pageResult = queryRecords(page, pageSize, activeIdList, periodYear, periodMonth);
            fillNames(pageResult.getRecords());
            PageResult<ScoreRecord> result = new PageResult<>();
            result.setTotal(pageResult.getTotal());
            result.setData(pageResult.getRecords());
            return ResponseResult.success(result);
        }

        // 指定了 employeeId，走原逻辑
        Page<ScoreRecord> pageResult = queryRecords(page, pageSize, null, periodYear, periodMonth);
        fillNames(pageResult.getRecords());
        PageResult<ScoreRecord> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setData(pageResult.getRecords());
        return ResponseResult.success(result);
    }

    /**
     * 通用分页查询
     * @param activeEmployeeIds 非空时只查这些员工的记录
     */
    private Page<ScoreRecord> queryRecords(Integer page, Integer pageSize, List<Long> activeEmployeeIds,
                                            Integer periodYear, Integer periodMonth) {
        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        if (activeEmployeeIds != null) {
            wrapper.in(ScoreRecord::getEmployeeId, activeEmployeeIds);
        }
        if (periodYear != null) {
            wrapper.eq(ScoreRecord::getPeriodYear, periodYear);
        }
        if (periodMonth != null) {
            wrapper.eq(ScoreRecord::getPeriodMonth, periodMonth);
        }
        wrapper.orderByDesc(ScoreRecord::getCreateTime);
        return scoreRecordService.page(new Page<>(page, pageSize), wrapper);
    }

    /**
     * 批量填充员工姓名和事件名称
     */
    private void fillNames(List<ScoreRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        // 批量查询员工姓名（仅启用员工）
        Set<Long> empIds = new HashSet<>();
        Set<Long> eventIds = new HashSet<>();
        for (ScoreRecord record : records) {
            if (record.getEmployeeId() != null) empIds.add(record.getEmployeeId());
            if (record.getEventId() != null) eventIds.add(record.getEventId());
        }
        Map<Long, String> empNameMap = sysEmployeeMapper.selectList(
                        new LambdaQueryWrapper<SysEmployee>()
                                .in(SysEmployee::getId, empIds)
                                .eq(SysEmployee::getStatus, 1)
                                .select(SysEmployee::getId, SysEmployee::getEmployeeName)
                )
                .stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getEmployeeName, (a, b) -> a));

        Map<Long, String> eventNameMap = eventIds.isEmpty() ? java.util.Collections.emptyMap() :
                scoreEventService.listByIds(eventIds).stream()
                        .collect(Collectors.toMap(ScoreEvent::getId, ScoreEvent::getEventName, (a, b) -> a));

        for (ScoreRecord record : records) {
            record.setEmployeeName(empNameMap.get(record.getEmployeeId()));
            record.setEventName(eventNameMap.get(record.getEventId()));
        }
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