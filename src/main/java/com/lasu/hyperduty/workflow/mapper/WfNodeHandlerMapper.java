package com.lasu.hyperduty.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.workflow.entity.WfNodeHandler;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 节点处理人配置 Mapper
 */
@Mapper
public interface WfNodeHandlerMapper extends BaseMapper<WfNodeHandler> {

    /**
     * 根据流程定义ID查询所有节点处理人配置
     * @param processDefinitionId 流程定义ID
     * @return 节点处理人配置列表
     */
    @Select("SELECT * FROM public.wf_node_handler WHERE process_definition_id = #{processDefinitionId} ORDER BY seq ASC, id ASC")
    List<WfNodeHandler> selectByProcessDefinitionId(@Param("processDefinitionId") String processDefinitionId);

    /**
     * 根据流程定义KEY查询最新版本的所有节点处理人配置
     * @param processDefinitionKey 流程定义KEY
     * @return 节点处理人配置列表
     */
    @Select("SELECT * FROM public.wf_node_handler WHERE process_definition_key = #{processDefinitionKey} ORDER BY seq ASC, id ASC")
    List<WfNodeHandler> selectByProcessDefinitionKey(@Param("processDefinitionKey") String processDefinitionKey);

    /**
     * 根据流程定义ID和节点ID查询配置
     */
    @Select("SELECT * FROM public.wf_node_handler WHERE process_definition_id = #{processDefinitionId} AND node_id = #{nodeId} LIMIT 1")
    WfNodeHandler selectByNode(@Param("processDefinitionId") String processDefinitionId, @Param("nodeId") String nodeId);

    /**
     * 根据流程定义ID删除所有节点配置
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM public.wf_node_handler WHERE process_definition_id = #{processDefinitionId}")
    int deleteByProcessDefinitionId(@Param("processDefinitionId") String processDefinitionId);
}
