package com.lasu.hyperduty.pm.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.pm.entity.PmTaskComment;
import com.lasu.hyperduty.pm.service.PmTaskCommentService;
import com.lasu.hyperduty.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;








/**
 * 任务批注控制器
 */
@RestController
@RequestMapping("/api/pm/task/comment")
public class PmTaskCommentController {

    @Autowired
    private PmTaskCommentService taskCommentService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取任务批注列表
     * @param taskId 任务ID
     * @return 批注列表
     */
    @GetMapping("/list")
    public ResponseResult<List<PmTaskComment>> getComments(@RequestParam Long taskId) {
        List<PmTaskComment> comments = taskCommentService.getCommentsByTaskId(taskId);
        return ResponseResult.success(comments);
    }

    /**
     * 添加任务批注
     * @param comment 批注信息
     * @return 是否添加成功
     */
    @PostMapping("/add")
    public ResponseResult<Boolean> addComment(@RequestBody PmTaskComment comment) {
        // 从JWT中获取当前用户信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Claims claims = jwtUtil.extractAllClaims(token);
            // 设置评论人信息
            if (claims.containsKey("employeeId")) {
                comment.setEmployeeId(Long.parseLong(claims.get("employeeId").toString()));
            }
            if (claims.containsKey("name")) {
                comment.setEmployeeName(claims.get("name").toString());
            }
        }

        boolean success = taskCommentService.addComment(comment);
        return ResponseResult.success(success);
    }

}
