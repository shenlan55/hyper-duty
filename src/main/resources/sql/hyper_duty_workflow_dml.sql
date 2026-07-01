-- P3-1 流程模板市场 DML
-- 内置 4 个常用模板：一键"使用此模板新建流程"
-- 注意：bpmn_xml 中保留模板特有的 process id 占位（如 leave_template_001），
--       实际"使用此模板"时前端会复制 xml 并把 process id 替换为新 KEY。

INSERT INTO public.wf_template (template_key, template_name, category, description, bpmn_xml, icon, sort_no, status, deleted, use_count, create_time, update_time)
VALUES
('tpl_general', '通用审批', 'general', '最简单的 1 级审批模板：开始→审批→结束，适合所有"提交后只需一人审批"的场景。',
 '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="general_template_001" name="通用审批" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始"/>
    <bpmn:userTask id="UserTask_1" name="审批"/>
    <bpmn:endEvent id="EndEvent_1" name="结束"/>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="UserTask_1"/>
    <bpmn:sequenceFlow id="Flow_2" sourceRef="UserTask_1" targetRef="EndEvent_1"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="general_template_001">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds x="152" y="102" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_2" bpmnElement="UserTask_1"><dc:Bounds x="240" y="80" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds x="392" y="102" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_1" bpmnElement="Flow_1"><di:waypoint x="188" y="120"/><di:waypoint x="240" y="120"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_2" bpmnElement="Flow_2"><di:waypoint x="340" y="120"/><di:waypoint x="392" y="120"/></bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>',
 'Document', 1, 1, 0, 0, NOW(), NOW()),

('tpl_leave', '请假申请', 'leave', '3 级审批：申请人直属上级 → 部门负责人 → HR 备案，适合所有请假场景。',
 '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="leave_template_001" name="请假申请" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始"/>
    <bpmn:userTask id="UserTask_leader" name="直属上级审批"/>
    <bpmn:userTask id="UserTask_dept" name="部门负责人审批"/>
    <bpmn:userTask id="UserTask_hr" name="HR 备案"/>
    <bpmn:endEvent id="EndEvent_1" name="结束"/>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="UserTask_leader"/>
    <bpmn:sequenceFlow id="Flow_2" sourceRef="UserTask_leader" targetRef="UserTask_dept"/>
    <bpmn:sequenceFlow id="Flow_3" sourceRef="UserTask_dept" targetRef="UserTask_hr"/>
    <bpmn:sequenceFlow id="Flow_4" sourceRef="UserTask_hr" targetRef="EndEvent_1"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="leave_template_001">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds x="100" y="120" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_leader" bpmnElement="UserTask_leader"><dc:Bounds x="200" y="100" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_dept" bpmnElement="UserTask_dept"><dc:Bounds x="360" y="100" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_hr" bpmnElement="UserTask_hr"><dc:Bounds x="520" y="100" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds x="680" y="120" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_1" bpmnElement="Flow_1"><di:waypoint x="136" y="138"/><di:waypoint x="200" y="140"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_2" bpmnElement="Flow_2"><di:waypoint x="300" y="140"/><di:waypoint x="360" y="140"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_3" bpmnElement="Flow_3"><di:waypoint x="460" y="140"/><di:waypoint x="520" y="140"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_4" bpmnElement="Flow_4"><di:waypoint x="620" y="140"/><di:waypoint x="680" y="138"/></bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>',
 'Calendar', 2, 1, 0, 0, NOW(), NOW()),

('tpl_reimburse', '费用报销', 'reimburse', '金额分支：≤1000 直属上级审批；>1000 需财务复核。',
 '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="reimburse_template_001" name="费用报销" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始"/>
    <bpmn:userTask id="UserTask_leader" name="直属上级审批"/>
    <bpmn:exclusiveGateway id="Gateway_amount" name="金额判断"/>
    <bpmn:userTask id="UserTask_finance" name="财务复核"/>
    <bpmn:endEvent id="EndEvent_1" name="结束"/>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="UserTask_leader"/>
    <bpmn:sequenceFlow id="Flow_2" sourceRef="UserTask_leader" targetRef="Gateway_amount"/>
    <bpmn:sequenceFlow id="Flow_3" name="≤1000" sourceRef="Gateway_amount" targetRef="EndEvent_1">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${amount &lt;= 1000}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_4" name="&gt;1000" sourceRef="Gateway_amount" targetRef="UserTask_finance">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">${amount &gt; 1000}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_5" sourceRef="UserTask_finance" targetRef="EndEvent_1"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="reimburse_template_001">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds x="100" y="120" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_leader" bpmnElement="UserTask_leader"><dc:Bounds x="190" y="100" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_Gateway_amount" bpmnElement="Gateway_amount" isMarkerVisible="true"><dc:Bounds x="345" y="115" width="50" height="50"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_finance" bpmnElement="UserTask_finance"><dc:Bounds x="450" y="180" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds x="620" y="120" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_1" bpmnElement="Flow_1"><di:waypoint x="136" y="138"/><di:waypoint x="190" y="140"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_2" bpmnElement="Flow_2"><di:waypoint x="290" y="140"/><di:waypoint x="345" y="140"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_3" bpmnElement="Flow_3"><di:waypoint x="395" y="140"/><di:waypoint x="620" y="138"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_4" bpmnElement="Flow_4"><di:waypoint x="370" y="165"/><di:waypoint x="370" y="220"/><di:waypoint x="450" y="220"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_5" bpmnElement="Flow_5"><di:waypoint x="550" y="220"/><di:waypoint x="620" y="156"/></bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>',
 'Money', 3, 1, 0, 0, NOW(), NOW()),

('tpl_trip', '出差申请', 'trip', '出差申请：部门负责人审批 → 行政备案。',
 '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="trip_template_001" name="出差申请" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始"/>
    <bpmn:userTask id="UserTask_dept" name="部门负责人审批"/>
    <bpmn:userTask id="UserTask_admin" name="行政备案"/>
    <bpmn:endEvent id="EndEvent_1" name="结束"/>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="UserTask_dept"/>
    <bpmn:sequenceFlow id="Flow_2" sourceRef="UserTask_dept" targetRef="UserTask_admin"/>
    <bpmn:sequenceFlow id="Flow_3" sourceRef="UserTask_admin" targetRef="EndEvent_1"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="trip_template_001">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1"><dc:Bounds x="120" y="120" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_dept" bpmnElement="UserTask_dept"><dc:Bounds x="220" y="100" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_UserTask_admin" bpmnElement="UserTask_admin"><dc:Bounds x="380" y="100" width="100" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_2" bpmnElement="EndEvent_1"><dc:Bounds x="540" y="120" width="36" height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_1" bpmnElement="Flow_1"><di:waypoint x="156" y="138"/><di:waypoint x="220" y="140"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_2" bpmnElement="Flow_2"><di:waypoint x="320" y="140"/><di:waypoint x="380" y="140"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="_BPMNEdge_Flow_3" bpmnElement="Flow_3"><di:waypoint x="480" y="140"/><di:waypoint x="540" y="138"/></bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>',
 'Promotion', 4, 1, 0, 0, NOW(), NOW())

ON CONFLICT (template_key) DO NOTHING;
