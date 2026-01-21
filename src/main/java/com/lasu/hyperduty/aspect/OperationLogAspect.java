package com.lasu.hyperduty.aspect;

import com.lasu.hyperduty.entity.OperationLog;
import com.lasu.hyperduty.service.OperationLogService;
import com.lasu.hyperduty.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作日志切面类
 * 用于自动记录系统操作日志
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 定义切点，拦截所有控制器方法
     */
    @Pointcut("execution(* com.lasu.hyperduty.controller.*.*(..))")
    public void operationLogPointcut() {
    }

    /**
     * 环绕通知，记录操作日志
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常信息
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        // 获取用户信息
        Long operatorId = null; // 默认值为null，避免外键约束失败
        String operatorName = "未知用户";

        // 从JWT中获取用户信息
        String token = request.getHeader("Authorization");
        String requestUrl = request.getRequestURI();
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = jwtUtil.extractAllClaims(token);
                // 检查claims中是否包含employeeId
                if (claims.containsKey("employeeId")) {
                    operatorId = Long.parseLong(claims.get("employeeId").toString());
                }
                // 检查claims中是否包含name
                if (claims.containsKey("name")) {
                    operatorName = claims.get("name").toString();
                }
            } catch (Exception e) {
                // JWT解析失败，使用默认值
            }
        } else if (requestUrl.equals("/api/auth/login")) {
            // 对于登录操作，从请求参数中获取用户名
            try {
                // 获取登录请求的参数
                String requestParams = getRequestParams(joinPoint, request);
                // 简单解析JSON格式的请求参数
                if (requestParams.contains("username")) {
                    // 提取username的值
                    int startIndex = requestParams.indexOf("username") + 9;
                    int endIndex = requestParams.indexOf(",", startIndex);
                    if (endIndex == -1) {
                        endIndex = requestParams.indexOf("}", startIndex);
                    }
                    if (startIndex < endIndex) {
                        String username = requestParams.substring(startIndex, endIndex).trim();
                        // 移除引号
                        username = username.replaceAll("\"", "");
                        operatorName = username;
                    }
                }
            } catch (Exception e) {
                // 解析失败，使用默认值
            }
        }

        // 获取操作类型
        String requestMethod = request.getMethod();
        String operationType = getOperationType(requestMethod);

        // 已经获取了操作模块的URL
        String operationModule = getOperationModule(requestUrl);

        // 对登录和登出操作进行特殊处理
        if (requestUrl.equals("/api/auth/login")) {
            operationType = "登录";
            operationModule = "认证管理";
        } else if (requestUrl.equals("/api/auth/logout")) {
            operationType = "登出";
            operationModule = "认证管理";
        }

        // 获取操作描述
        String operationDesc = getOperationDesc(joinPoint);

        // 获取请求参数
        String requestParams = getRequestParams(joinPoint, request);

        // 获取IP地址
        String ipAddress = getIpAddress(request);

        // 获取用户代理
        String userAgent = request.getHeader("User-Agent");

        Object result = null;
        int status = 1; // 默认成功
        String errorMsg = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Exception e) {
            // 记录错误信息
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // 计算执行时间
            int executionTime = (int) (System.currentTimeMillis() - startTime);

            // 处理响应结果，限制长度避免超出数据库字段限制
            String responseResult = null;
            if (result != null) {
                String resultStr = result.toString();
                // 限制响应结果长度为1000个字符
                responseResult = resultStr.length() > 1000 ? resultStr.substring(0, 1000) + "..." : resultStr;
            }

            // 记录操作日志
            operationLogService.logOperation(
                    operatorId,
                    operatorName,
                    operationType,
                    operationModule,
                    operationDesc,
                    requestMethod,
                    requestUrl,
                    requestParams,
                    responseResult,
                    ipAddress,
                    userAgent,
                    executionTime,
                    status,
                    errorMsg
            );
        }

        return result;
    }

    /**
     * 根据HTTP方法获取操作类型
     * @param requestMethod HTTP方法
     * @return 操作类型
     */
    private String getOperationType(String requestMethod) {
        switch (requestMethod) {
            case "GET":
                return "查询";
            case "POST":
                return "添加";
            case "PUT":
            case "PATCH":
                return "修改";
            case "DELETE":
                return "删除";
            default:
                return "其他";
        }
    }

    /**
     * 根据请求URL获取操作模块
     * @param requestUrl 请求URL
     * @return 操作模块
     */
    private String getOperationModule(String requestUrl) {
        // 移除/api前缀（如果存在）
        if (requestUrl.startsWith("/api")) {
            requestUrl = requestUrl.substring(4);
        }
        
        // 处理系统管理模块
        if (requestUrl.startsWith("/system/dept")) {
            return "部门管理";
        } else if (requestUrl.startsWith("/system/employee")) {
            return "人员管理";
        } else if (requestUrl.startsWith("/system/user")) {
            return "用户管理";
        } else if (requestUrl.startsWith("/system/menu")) {
            return "菜单管理";
        } else if (requestUrl.startsWith("/system/role")) {
            return "角色管理";
        } else if (requestUrl.startsWith("/system/dict")) {
            return "字典管理";
        } else if (requestUrl.startsWith("/system/operation-log")) {
            return "操作日志";
        }
        
        // 处理值班管理模块
        if (requestUrl.startsWith("/duty/schedule")) {
            return "值班表管理";
        } else if (requestUrl.startsWith("/duty/assignment")) {
            return "值班安排";
        } else if (requestUrl.startsWith("/duty/record")) {
            return "值班记录";
        } else if (requestUrl.startsWith("/duty/shift-config")) {
            return "班次配置";
        } else if (requestUrl.startsWith("/duty/leave-request")) {
            return "请假申请";
        } else if (requestUrl.startsWith("/duty/leave-approval")) {
            return "请假审批";
        } else if (requestUrl.startsWith("/duty/swap-request")) {
            return "调班管理";
        } else if (requestUrl.startsWith("/duty/auto-schedule")) {
            return "自动排班";
        } else if (requestUrl.startsWith("/duty/statistics")) {
            return "排班统计";
        } else if (requestUrl.startsWith("/duty/schedule-mode")) {
            return "排班模式";
        } else if (requestUrl.startsWith("/duty/operation-log")) {
            return "操作日志";
        }
        
        // 处理其他模块
        if (requestUrl.startsWith("/dept")) {
            return "部门管理";
        } else if (requestUrl.startsWith("/employee")) {
            return "人员管理";
        } else if (requestUrl.startsWith("/user")) {
            return "用户管理";
        } else if (requestUrl.startsWith("/role")) {
            return "角色管理";
        } else if (requestUrl.startsWith("/menu")) {
            return "菜单管理";
        } else if (requestUrl.startsWith("/auth")) {
            return "认证管理";
        } else if (requestUrl.startsWith("/dict")) {
            return "字典管理";
        } else {
            return "其他模块";
        }
    }

    /**
     * 获取操作描述
     * @param joinPoint 连接点
     * @return 操作描述
     */
    private String getOperationDesc(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getName();
    }

    /**
     * 获取请求参数
     * @param joinPoint 连接点
     * @param request 请求对象
     * @return 请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        if (request.getMethod().equals("GET")) {
            // GET请求，从请求参数中获取
            return request.getParameterMap().entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                    .collect(Collectors.joining("&"));
        } else {
            // POST/PUT/DELETE请求，从方法参数中获取
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                return args[0].toString();
            }
            return "";
        }
    }

    /**
     * 获取IP地址
     * @param request 请求对象
     * @return IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理X-Forwarded-For中可能包含多个IP地址的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // 处理IPv6本地回环地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        
        return ip;
    }
}