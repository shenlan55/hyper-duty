package com.lasu.hyperduty.workflow.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 流程版本对比 DTO
 * ----------------------------------------------------------------------------
 * 比较两个 deploymentId 对应的 BPMN XML 差异
 * - nodeDiffs: 节点差异（userTask / serviceTask / gateway / event）
 * - flowDiffs: 连线差异（sequenceFlow）
 * - 状态：ADDED / REMOVED / MODIFIED / UNCHANGED
 * ----------------------------------------------------------------------------
 */
@Data
public class WfDefinitionDiffDTO {

    private String deploymentIdA;
    private String deploymentIdB;
    private String versionA;
    private String versionB;

    /** 节点差异 */
    private List<DiffNode> nodeDiffs;
    /** 连线差异 */
    private List<DiffNode> flowDiffs;

    @Data
    public static class DiffNode {
        /** 元素 id（如 UserTask_Approve） */
        private String id;
        /** 元素名（显示用） */
        private String name;
        /** 类型：userTask / serviceTask / gateway / event / sequenceFlow */
        private String type;
        /** 状态：ADDED / REMOVED / MODIFIED / UNCHANGED */
        private String status;
        /** 改动点（key:value 多项） */
        private Map<String, String[]> changes;
    }
}
