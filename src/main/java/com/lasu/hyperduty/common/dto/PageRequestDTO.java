package com.lasu.hyperduty.common.dto;

import com.lasu.hyperduty.common.dto.PageRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;







/**
 * 分页请求DTO
 */
@Data
@Schema(description = "分页请求对象")
public class PageRequestDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键字", example = "")
    private String keyword;

}