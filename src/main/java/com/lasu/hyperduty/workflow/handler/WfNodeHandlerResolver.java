package com.lasu.hyperduty.workflow.handler;

import com.lasu.hyperduty.workflow.entity.WfNodeHandler;
import com.lasu.hyperduty.workflow.mapper.WfNodeHandlerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 节点处理人解析器（P1-10）
 * ----------------------------------------------------------------------------
 * 根据 wf_node_handler.handlerType 把"逻辑占位符"解析为真实的 assignee / candidate users。
 *
 * 支持的 handlerType：
 *  - ASSIGNEE            : 固定人员（handlerConfig 存 employeeId，多个逗号分隔）
 *  - CANDIDATE_USERS     : 候选人员（handlerConfig 存 employeeId 列表）
 *  - CANDIDATE_GROUPS    : 候选组/角色（handlerConfig 存 roleKey 列表）
 *  - INITIATOR           : 发起人
 *  - DEPT_LEADER         : 发起人所在部门负责人（handlerConfig 存 deptId 列表，逗号分隔；
 *                          留空则用发起人所在部门；多个则并集）
 *  - ROLE_LEADER         : 角色负责人（handlerConfig 存 roleId 列表，多个并集）
 *  - PREV_ASSIGNEE       : 上一节点处理人
 *  - FORM_FIELD          : 表单字段（handlerConfig 存 {field:"xxx"}，从 task 变量中读）
 *  - VARIABLE            : 流程变量（handlerConfig 存 {varName:"xxx"}，从 process 变量中读）
 *
 * 注：本类只做"计算"和"set 到 task"，具体的人员库查询（SysEmployee / SysDept / SysRole）由
 * WfPersonResolver 集中负责，避免本类耦合到组织架构模块。
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WfNodeHandlerResolver {

    private final WfNodeHandlerMapper wfNodeHandlerMapper;
    private final WfPersonResolver wfPersonResolver;

    /**
     * 在 task create 时被调用：根据 node handler 配置设置 assignee / candidate
     */
    public void resolveAndApply(DelegateTask task) {
        String processDefinitionId = task.getProcessDefinitionId();
        String nodeId = task.getTaskDefinitionKey();
        WfNodeHandler handler = wfNodeHandlerMapper.selectByNode(processDefinitionId, nodeId);
        if (handler == null) {
            log.debug("[节点处理人] 未配置 handler: processDefinitionId={}, nodeId={}", processDefinitionId, nodeId);
            return;
        }
        String type = handler.getHandlerType();
        String config = handler.getHandlerConfig();
        log.info("[节点处理人] 开始解析 type={}, nodeId={}, config={}", type, nodeId, config);
        switch (type == null ? "" : type) {
            case "ASSIGNEE": {
                List<String> assignees = wfPersonResolver.toUsernames(parseEmployeeIds(config), task);
                if (!assignees.isEmpty()) {
                    applyAssignee(task, assignees.get(0));
                }
                break;
            }
            case "CANDIDATE_USERS":
                addCandidateUsers(task, wfPersonResolver.toUsernames(parseEmployeeIds(config), task));
                break;
            case "CANDIDATE_GROUPS":
                addCandidateGroups(task, parseRoleKeys(config));
                break;
            case "INITIATOR":
                applyAssignee(task, resolveInitiator(task));
                break;
            case "DEPT_LEADER":
                addCandidateUsers(task, wfPersonResolver.findDeptLeaders(parseDeptIds(config), task));
                break;
            case "ROLE_LEADER":
                addCandidateUsers(task, wfPersonResolver.findRoleLeaders(parseRoleIds(config), task));
                break;
            case "PREV_ASSIGNEE":
                applyAssignee(task, resolvePrevAssignee(task));
                break;
            case "FORM_FIELD":
                applyAssignee(task, resolveFromFormField(config, task));
                break;
            case "VARIABLE":
                applyAssignee(task, resolveFromVariable(config, task));
                break;
            default:
                log.warn("[节点处理人] 未知 handlerType: {}", type);
        }
    }

    /** 设置单人 assignee（取第一个） */
    private void applyAssignee(DelegateTask task, String username) {
        if (username == null || username.isEmpty()) {
            log.warn("[节点处理人] 计算结果为空，跳过 assignee: taskId={}", task.getId());
            return;
        }
        task.setAssignee(username);
        log.info("[节点处理人] setAssignee: taskId={}, assignee={}", task.getId(), username);
    }

    /** 加候选组/人员 */
    private void addCandidateUsers(DelegateTask task, java.util.List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            log.warn("[节点处理人] 候选用户结果为空: taskId={}", task.getId());
            return;
        }
        for (String u : usernames) {
            if (u != null && !u.isEmpty()) {
                task.addCandidateUser(u);
            }
        }
        log.info("[节点处理人] addCandidateUsers: taskId={}, users={}", task.getId(), usernames);
    }

    private void addCandidateGroups(DelegateTask task, java.util.List<String> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        for (String g : groups) {
            if (g != null && !g.isEmpty()) {
                task.addCandidateGroup(g);
            }
        }
    }

    /**
     * INITIATOR：发起人作为 assignee
     * 优先用 INITIATOR 流程变量，否则用发起人 ID
     */
    private String resolveInitiator(DelegateTask task) {
        Object v = task.getVariable("INITIATOR");
        return v == null ? null : v.toString();
    }

    /**
     * PREV_ASSIGNEE：上一已完成 UserTask 的 assignee
     * 实现方式：从 task 局部变量或 process 变量读 `__prev_assignee__`
     */
    @SuppressWarnings("unchecked")
    private String resolvePrevAssignee(DelegateTask task) {
        Object var = task.getVariable("__prev_assignee__");
        if (var != null) return var.toString();
        // 也允许从 process 变量读（流程启动时由其它 listener 写入）
        return null;
    }

    /**
     * FORM_FIELD：从 task 变量（form 提交数据）读 userId 字段
     * config 示例：{"field":"approveUserId"}
     */
    private String resolveFromFormField(String config, DelegateTask task) {
        String field = extractJsonString(config, "field");
        if (field == null) return null;
        Object v = task.getVariable(field);
        return v == null ? null : v.toString();
    }

    /**
     * VARIABLE：从 process 变量读
     * config 示例：{"var":"nextApprover"}
     */
    private String resolveFromVariable(String config, DelegateTask task) {
        String var = extractJsonString(config, "var");
        if (var == null) return null;
        Object v = task.getVariable(var);
        return v == null ? null : v.toString();
    }

    // ----------------------- 简单解析工具 -----------------------

    private java.util.List<String> parseEmployeeIds(String config) {
        return splitCsv(config);
    }

    private java.util.List<String> parseDeptIds(String config) {
        return splitCsv(config);
    }

    private java.util.List<String> parseRoleIds(String config) {
        return splitCsv(config);
    }

    private java.util.List<String> parseRoleKeys(String config) {
        return splitCsv(config);
    }

    private java.util.List<String> splitCsv(String s) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (s == null || s.trim().isEmpty()) return out;
        for (String p : s.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    /**
     * 极简 JSON 字段提取（field / var），避免引入 Jackson 依赖
     * 匹配 "key":"value" 或 "key":value（无引号）
     */
    private static final Pattern JSON_FIELD = Pattern.compile("\"(field|var|value)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}]+))");

    private String extractJsonString(String config, String key) {
        if (config == null) return null;
        Matcher m = JSON_FIELD.matcher(config);
        while (m.find()) {
            if (key.equals(m.group(1))) {
                String v = m.group(2) != null ? m.group(2) : m.group(3);
                return v == null ? null : v.trim();
            }
        }
        return null;
    }
}
