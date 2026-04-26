package com.lasu.hyperduty.common.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;








/**
 * 分页响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应对象")
public class PageResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long current;

    @Schema(description = "每页大小")
    private long size;

    @Schema(description = "总页数")
    private long pages;

    /**
     * 从MyBatis Plus的Page对象转换
     * @param page MyBatis Plus的Page对象
     * @param <T> 数据类型
     * @return 分页响应DTO
     */
    public static <T> PageResponseDTO<T> fromPage(Page<T> page) {
        PageResponseDTO<T> response = new PageResponseDTO<>();
        response.setRecords(page.getRecords());
        response.setTotal(page.getTotal());
        response.setCurrent(page.getCurrent());
        response.setSize(page.getSize());
        response.setPages(page.getPages());
        return response;
    }

}