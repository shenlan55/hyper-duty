package com.lasu.hyperduty.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.common.dto.PageRequestDTO;
import com.lasu.hyperduty.common.dto.PageResponseDTO;
import com.lasu.hyperduty.common.service.BasePageService;
import java.util.Map;








/**
 * 基础分页服务接口
 * @param <T> 实体类型
 */
public interface BasePageService<T> extends IService<T> {

    /**
     * 分页查询
     * @param pageRequestDTO 分页请求DTO
     * @param params 额外查询参数
     * @return 分页响应DTO
     */
    PageResponseDTO<T> page(PageRequestDTO pageRequestDTO, Map<String, Object> params);

    /**
     * 从MyBatis Plus的Page对象转换为PageResponseDTO
     * @param page MyBatis Plus的Page对象
     * @param <T> 数据类型
     * @return 分页响应DTO
     */
    default <T> PageResponseDTO<T> toPageResponse(Page<T> page) {
        return PageResponseDTO.fromPage(page);
    }

    /**
     * 创建MyBatis Plus的Page对象
     * @param pageRequestDTO 分页请求DTO
     * @param <T> 数据类型
     * @return MyBatis Plus的Page对象
     */
    default <T> Page<T> createPage(PageRequestDTO pageRequestDTO) {
        return new Page<>(pageRequestDTO.getPageNum(), pageRequestDTO.getPageSize());
    }

}