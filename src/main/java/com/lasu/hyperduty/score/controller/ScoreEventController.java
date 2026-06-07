package com.lasu.hyperduty.score.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.score.entity.ScoreEvent;
import com.lasu.hyperduty.score.service.ScoreEventService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分事件 Controller
 */
@RestController
@RequestMapping("/score/events")
public class ScoreEventController {

    @Autowired
    private ScoreEventService scoreEventService;

    /**
     * 查询所有启用的事件
     */
    @GetMapping
    public ResponseResult<List<ScoreEvent>> list() {
        List<ScoreEvent> list = scoreEventService.lambdaQuery()
                .orderByAsc(ScoreEvent::getSort)
                .list();
        return ResponseResult.success(list);
    }

    /**
     * 新增积分事件
     */
    @PostMapping
    public ResponseResult<String> create(@RequestBody ScoreEvent event) {
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());
        scoreEventService.save(event);
        return ResponseResult.success("新增成功");
    }

    /**
     * 修改积分事件
     */
    @PutMapping("/{id}")
    public ResponseResult<String> update(@PathVariable Long id, @RequestBody ScoreEvent event) {
        event.setId(id);
        event.setUpdateTime(LocalDateTime.now());
        scoreEventService.updateById(event);
        return ResponseResult.success("修改成功");
    }

    /**
     * 删除积分事件
     */
    @DeleteMapping("/{id}")
    public ResponseResult<String> delete(@PathVariable Long id) {
        scoreEventService.removeById(id);
        return ResponseResult.success("删除成功");
    }
}