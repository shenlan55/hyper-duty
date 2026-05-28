---
name: "hyper-duty-fullstack"
description: "Hyper Duty 全栈开发专家，涵盖前端、后端和 DevOps，使用 Spring Boot 3、Vue 3、MyBatis、Element Plus 等技术栈。"
---

# Hyper Duty 全栈开发专家

这个技能专门用于开发和维护 Hyper Duty 系统，涵盖前端、后端和 DevOps 全链路开发。

## 核心职责

### 后端开发
- **API 开发**：使用 Spring Web 创建 RESTful API
- **数据库集成**：使用 MyBatis 进行数据库操作
- **安全配置**：配置 Spring Security 和 JWT 认证
- **服务层实现**：开发业务逻辑
- **实体管理**：创建和维护数据库实体
- **异常处理**：实现统一异常处理
- **性能优化**：优化数据库查询和服务方法
- **定时任务**：配置和维护 Spring Scheduling 定时任务
- **批量操作**：实现大数据集的批量处理
- **缓存管理**：使用 @Cacheable、CacheableServiceImpl 基类和 CacheUtil 工具类实现 Redis 缓存
- **限流保护**：使用 @RateLimit 注解实现 API 限流
- **安全加固**：实现 XSS 防护等安全措施

### 前端开发
- **组件开发**：使用 Vue 3 Composition API 创建组件
- **API 集成**：使用 Axios 实现 API 调用，遵循简化响应处理
- **UI 实现**：使用 Element Plus 构建响应式界面
- **状态管理**：使用 Pinia 管理应用状态
- **路由配置**：设置和维护 Vue Router 配置
- **表单验证**：实现客户端表单验证
- **错误处理**：创建统一错误处理机制
- **性能优化**：优化前端性能和加载速度
- **组件复用**：使用和扩展 BaseTable、VirtualList 组件实现统一 UI
- **安全防护**：使用 xssUtil.js 实现 XSS 防护
- **代码质量**：确保一致的代码风格和最佳实践

### DevOps
- **项目设置**：配置开发环境和项目依赖
- **构建流程**：管理前端和后端构建流程
- **部署管理**：处理应用到不同环境的部署
- **集成测试**：协调前后端集成测试
- **数据库管理**：处理数据库迁移和种子数据
- **监控设置**：设置应用监控和日志
- **性能测试**：进行负载测试和性能优化
- **安全扫描**：执行安全漏洞扫描

## 关键文件和目录

### 后端核心文件
```
src/main/java/com/lasu/hyperduty/
├── HyperDutyApplication.java          # 应用入口
├── config/                             # 配置类
│   ├── ThreadPoolConfig.java
│   ├── ScheduleConfig.java
│   ├── PasswordEncoderConfig.java
│   └── RedisConfig.java
├── security/                           # 安全组件
│   ├── SecurityConfig.java
│   └── JwtAuthenticationFilter.java
├── annotation/                         # 自定义注解
│   └── RateLimit.java
├── aspect/                             # AOP 切面
│   ├── RateLimitAspect.java
│   └── OperationLogAspect.java
├── controller/                         # REST 控制器
│   ├── AuthController.java
│   ├── SysEmployeeController.java
│   ├── SysDeptController.java
│   ├── SysRoleController.java
│   └── SysMenuController.java
├── service/                            # 服务接口
├── service/impl/                       # 服务实现
│   └── CacheableServiceImpl.java      # 缓存基类
├── entity/                             # 数据库实体
├── mapper/                             # MyBatis Mapper 接口
├── common/                             # 通用工具
│   ├── ResponseResult.java
│   ├── ResponseUtil.java
│   └── PageResult.java
└── utils/
    └── CacheUtil.java
```

### 前端核心文件
```
frontend/src/
├── main.js                             # 应用入口
├── router/index.js                     # 路由配置
├── stores/index.js                     # Pinia Store 设置
├── utils/
│   ├── request.js                      # Axios 拦截器配置
│   └── xssUtil.js                      # XSS 防护工具
├── components/
│   ├── BaseTable.vue                   # 统一表格组件（含分页和排序）
│   └── VirtualList.vue                 # 长列表虚拟滚动组件
├── views/
│   ├── Login.vue
│   ├── Dashboard.vue
│   ├── Employee.vue
│   ├── Dept.vue
│   ├── Role.vue
│   └── Menu.vue
└── api/
    ├── auth.js
    ├── employee.js
    ├── dept.js
    ├── role.js
    └── menu.js
```

### 数据库文件
```
src/main/resources/sql/
├── hyper_duty_ddl.sql                  # 数据库结构
└── hyper_duty_dml.sql                  # 种子数据
```

### 构建配置
- `pom.xml` - Maven 后端构建配置
- `frontend/package.json` - npm 前端包配置
- `frontend/vite.config.js` - Vite 构建配置

## 开发规范

### 后端开发规范

#### 1. API 设计
遵循 RESTful API 设计原则：
- 使用合适的 HTTP 方法（GET、POST、PUT、DELETE）
- 实现适当的错误处理
- 返回统一的响应格式

```java
// 统一响应格式
@Data
public class ResponseResult<T> {
    private Integer code;
    private String message;
    private T data;
}
```

#### 2. 数据库操作
使用 MyBatis 最佳实践：
- 使用参数化查询防止 SQL 注入
- 实现适当的事务管理
- 使用索引和连接优化查询

#### 3. 安全实践
遵循 Spring Security 最佳实践：
- 使用 BCrypt 进行密码哈希
- 实现适当的 JWT Token 管理
- 配置安全的 CORS 设置

#### 4. 缓存管理
- **缓存键设计**：使用 `{module}::{key}_{params}` 格式
- **缓存清除机制**：
  - 简单场景：使用 @CacheEvict 注解
  - 复杂场景：继承 CacheableServiceImpl 并实现 clearCache 方法
  - 特殊场景：使用 CacheUtil 工具类
- **序列化配置**：确保 Redis 序列化支持 Java 8 日期时间类型
- **缓存一致性**：创建/更新/删除操作后始终清除相关缓存
- **缓存监控**：定期检查 Redis 缓存使用情况避免膨胀

#### 5. 异常处理
实现统一异常处理：
- 创建自定义异常类
- 使用 @ControllerAdvice 进行全局错误处理
- 返回一致的错误响应

### 前端开发规范

#### 1. API 响应处理
使用简化模式，成功响应直接返回 `data.data`：
```javascript
const data = await apiFunction();
data.value = data || [];
```

#### 2. 组件结构
遵循 Vue 3 Composition API 最佳实践：
- 使用 `<script setup>` 简化开发
- 按功能组织代码
- 合理使用响应式状态

#### 3. 表单处理
使用 Element Plus 表单组件配合适当验证：
- 实现实时验证
- 提供清晰的错误消息
- 处理表单提交状态

#### 4. 事件处理与防抖
**事件处理**：
- 使用 Vue 事件修饰符控制事件行为：
  - `.prevent`：阻止默认行为
  - `.stop`：阻止事件冒泡
  - `.native`：监听组件根元素原生事件
- 对于弹窗表单，在 el-dialog、el-form、el-input 多个层面拦截回车键事件，防止误操作关闭弹窗或触发默认提交

**防抖机制**：
- 使用状态变量防止重复提交（如 `isSubmitting`、`isVerifying`）
- 在 `try-finally` 块中确保状态变量被正确重置
- 避免在异步操作完成前允许重复触发

**代码示例**：
```vue
<template>
  <!-- 弹窗层面拦截回车键 -->
  <el-dialog @keyup.enter.prevent.stop>
    <!-- 表单层面拦截回车键和默认提交 -->
    <el-form @submit.prevent @keyup.enter.prevent.stop>
      <!-- 输入框层面拦截回车键 -->
      <el-input 
        @keydown.enter.prevent.stop="handleVerifyCode"
        @keyup.enter.prevent.stop="handleVerifyCode"
      />
    </el-form>
  </el-dialog>
</template>

<script setup>
const isSubmitting = ref(false)

const handleSubmit = async () => {
  if (isSubmitting.value) return  // 防抖检查
  try {
    isSubmitting.value = true
    // 执行提交逻辑
  } finally {
    isSubmitting.value = false  // 确保状态重置
  }
}
</script>
```

**常见问题及解决方案**：
1. **问题**：在验证码弹窗按回车直接关闭
   **解决**：在 el-dialog、el-form、el-input 同时使用 `@keyup.enter.prevent.stop`

2. **问题**：一次回车触发两次请求
   **解决**：移除外层 el-form 的重复事件监听，只在输入框层面处理

3. **问题**：用户快速多次点击提交按钮
   **解决**：使用防抖状态变量防止重复请求

#### 4. 状态管理
使用 Pinia 进行全局状态：
- 保持组件尽可能无状态
- 使用 Stores 管理共享数据
- 实现适当的 Store 结构

#### 5. 组件复用
- 使用 BaseTable 组件实现统一的表格样式和功能
- 使用 VirtualList 组件优化长列表性能
- 使用 xssUtil.js 处理用户输入防止 XSS 攻击

### DevOps 规范

#### 1. 后端开发流程
1. **环境设置**：安装 JDK 17 和 Maven 3.8+
2. **依赖安装**：运行 `mvn clean install` 安装依赖
3. **数据库初始化**：执行 SQL 脚本设置数据库
4. **本地开发**：运行 `mvn spring-boot:run` 启动后端服务
5. **测试**：运行 `mvn test` 执行单元测试
6. **构建**：运行 `mvn clean package` 构建后端产物

#### 2. 前端开发流程
1. **环境设置**：安装 Node.js 16.x 或 18.x
2. **依赖安装**：在 frontend 目录运行 `npm install`
3. **本地开发**：运行 `npm run dev` 启动前端开发服务器
4. **测试**：运行 `npm test` 执行前端测试
5. **构建**：运行 `npm run build` 构建前端产物

#### 3. 集成测试流程
1. **启动后端**：在 localhost:8080 运行后端服务
2. **启动前端**：在 localhost:5173 运行前端服务
3. **API 测试**：使用 Postman 或 curl 测试 REST API
4. **端到端测试**：测试前后端完整的用户流程
5. **性能测试**：测试负载下的系统性能

#### 4. 部署流程
1. **构建产物**：构建前端和后端产物
2. **Docker 构建**：为两个服务构建 Docker 镜像
3. **Docker Compose**：使用 docker-compose 编排服务
4. **环境配置**：为目标环境设置环境变量
5. **部署**：部署到目标环境
6. **健康检查**：验证服务正常运行

## 常见问题和解决方案

### 后端问题
1. **循环依赖**：使用 setter 注入或 @Lazy 注解
2. **JWT Token 问题**：检查令牌过期和签名验证
3. **数据库性能**：优化查询并使用适当的索引
4. **安全配置**：确保适当的访问控制和身份验证
5. **定时任务问题**：检查任务调度配置和线程池设置
6. **端口占用**：停止冲突进程或更改端口
7. **数据库连接失败**：检查数据库凭据和网络连接
8. **依赖解析失败**：清除 Maven 缓存并重试

### 前端问题
1. **数据显示问题**：确保 API 调用遵循简化模式
2. **表单验证错误**：检查 Element Plus 表单规则和绑定
3. **路由导航问题**：验证 Vue Router 配置和守卫
4. **状态管理错误**：检查 Pinia Store mutations 和 actions
5. **性能问题**：优化组件渲染和 API 调用
6. **CORS 错误**：检查后端 CORS 配置
7. **构建失败**：检查语法错误和缺失依赖
8. **API 响应错误**：验证 API 端点和响应格式
9. **UI 渲染问题**：检查 Vue 组件语法和 Element Plus 使用

## 测试指南

### 后端测试
- 为服务方法编写单元测试
- 为 API 端点实现集成测试
- 测试错误处理场景
- 验证安全配置
- 使用不同的数据集测试数据库操作

### 前端测试
- 测试不同数据状态下的组件渲染
- 验证表单验证和提交
- 使用模拟数据测试 API 集成
- 确保响应式设计跨设备工作
- 测试错误处理场景

### 集成测试检查清单
1. **API 集成**：验证所有前端 API 调用正常工作
2. **认证流程**：测试完整的登录/登出流程
3. **CRUD 操作**：测试创建、读取、更新、删除操作
4. **错误处理**：测试错误场景和错误消息
5. **性能**：测试正常负载下的系统响应时间
6. **安全性**：测试身份验证和授权机制
7. **数据完整性**：验证前后端数据一致性
8. **边界情况**：测试边界条件和边缘情况

## 部署检查清单

1. **环境配置**：验证环境变量
2. **数据库设置**：确保数据库正确配置
3. **服务连接**：验证服务可以通信
4. **SSL 配置**：确保 HTTPS 正确配置
5. **负载均衡**：验证负载均衡配置
6. **监控设置**：确保监控到位
7. **备份配置**：验证备份流程
8. **灾难恢复**：测试灾难恢复程序

## 示例工作流程

### 后端特性开发
1. **实体创建**：为新特性创建数据库实体
2. **Mapper 实现**：实现 MyBatis Mappers 进行数据库操作
3. **服务开发**：创建服务接口和实现
4. **控制器实现**：开发 RESTful API 控制器
5. **安全配置**：如果需要更新安全设置
6. **测试**：为新功能编写和运行测试
7. **部署**：准备部署和监控

### 前端组件开发
1. **组件创建**：创建结构正确的新 Vue 组件
2. **API 集成**：使用简化模式实现 API 调用
3. **UI 实现**：使用 Element Plus 构建界面
4. **状态管理**：如果需要设置 Pinia Store
5. **测试**：测试组件功能和边缘情况
6. **优化**：优化性能和用户体验

## 最佳实践

### 代码组织
- 遵循模块化设计原则
- 添加公共方法的 Javadoc 注释
- 实现全面的日志记录
- 设置应用监控
- 定期进行代码审查

### 性能优化
- **数据库优化**：使用适当索引并优化查询
- **后端优化**：使用缓存和线程池
- **前端优化**：压缩资源并优化图像
- **部署优化**：使用负载均衡和自动扩展

### 安全最佳实践
- **依赖扫描**：定期扫描依赖项的漏洞
- **安全配置**：遵循安全配置实践
- **访问控制**：实现适当的访问控制机制
- **加密**：使用 HTTPS 并加密敏感数据
- **审计日志**：维护全面的审计日志

## 项目规则参考

始终参考 `.trae/rules/project_rules.md`：
- 技术栈约束（仅允许使用指定技术栈）
- API 调用模式（简化响应处理）
- 缓存管理规范
- 代码风格约定
- 分页实现规范
- 表格组件使用规范
- XSS 防护规范

---

## 快速开始

当你被调用进行开发时：
1. **确认需求范围**：明确是前端、后端还是全栈任务
2. **遵循技术栈**：严格按照项目规则使用指定技术
3. **参考现有模块**：查看已实现的功能作为参考
4. **保持沟通**：每个阶段完成后与用户确认后再继续
5. **确保代码质量**：遵循最佳实践和项目规范

让我们一起构建出色的 Hyper Duty 系统！🚀
