package com.lasu.hyperduty.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * HTTP响应工具类，用于统一处理响应
 */
public class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送JSON响应
     * @param response HTTP响应对象
     * @param status 状态码
     * @param responseResult 响应结果对象
     */
    public static void sendJsonResponse(HttpServletResponse response, int status, ResponseResult<?> responseResult) {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            String json = objectMapper.writeValueAsString(responseResult);
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送错误响应
     * @param response HTTP响应对象
     * @param status 状态码
     * @param message 错误信息
     */
    public static void sendErrorResponse(HttpServletResponse response, int status, String message) {
        ResponseResult<?> responseResult = ResponseResult.error(status, message);
        sendJsonResponse(response, status, responseResult);
    }

    /**
     * 发送未授权响应
     * @param response HTTP响应对象
     * @param message 错误信息
     */
    public static void sendUnauthorizedResponse(HttpServletResponse response, String message) {
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    /**
     * 发送服务器内部错误响应
     * @param response HTTP响应对象
     * @param message 错误信息
     */
    public static void sendInternalErrorResponse(HttpServletResponse response, String message) {
        sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 发送资源不存在响应
     * @param response HTTP响应对象
     * @param message 错误信息
     */
    public static void sendNotFoundResponse(HttpServletResponse response, String message) {
        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, message);
    }

    /**
     * 发送禁止访问响应
     * @param response HTTP响应对象
     * @param message 错误信息
     */
    public static void sendForbiddenResponse(HttpServletResponse response, String message) {
        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, message);
    }
}