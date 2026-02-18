package com.lasu.hyperduty.dto;

import lombok.Data;

/**
 * 刷新令牌请求DTO
 */
@Data
public class RefreshTokenDTO {
    /**
     * 刷新令牌
     */
    private String refreshToken;
}
