# 超级值班系统

## 项目概述

超级值班系统（Hyper Duty System）是一款面向企业的值班管理系统，用于管理员工值班信息、排班安排、值班记录等功能，提高企业值班管理效率。

## 技术栈

- **后端**：Spring Boot 3.1.0 + Spring Security + MyBatis Plus 3.5.6 + Druid + MySQL 8.0
- **前端**：Vue 3 + Vite 4.5.0 + Element Plus + Pinia + Vue Router

## 功能模块

### 1. 用户管理
- 员工信息管理：添加、修改、删除员工信息
- 部门管理：部门树形结构管理
- 角色管理：角色创建、编辑、删除
- 权限管理：基于角色的菜单权限控制

### 2. 值班管理
- 排班管理：创建、修改、删除排班计划
- 值班记录：记录值班情况，包括签到、签退、异常情况
- 值班统计：按时间段、员工、部门统计值班情况

### 3. 菜单管理
- 菜单树形结构管理
- 菜单权限分配：为不同角色分配不同菜单权限

### 4. 认证授权
- 用户登录：JWT令牌认证
- 权限控制：基于角色的访问控制

## 启动步骤

1. **初始化数据库**：执行 `src/main/resources/sql/init.sql` 创建数据库和表
2. **启动后端**：
   ```
   mvn spring-boot:run
   # 或
   java -jar target/hyper-duty-1.0.0.jar
   ```
3. **启动前端**：
   ```
   cd frontend
   npm run dev
   ```
4. **访问应用**：
   - 前端：http://localhost:5173
   - 登录账号：admin/123456

## 项目结构

### 后端项目结构
```
hyper-duty/
├── src/main/java/com/lasu/hyperduty/
│   ├── common/           # 公共组件
│   ├── config/           # 配置类
│   ├── controller/       # 控制器层
│   ├── entity/           # 实体类
│   ├── mapper/           # Mapper接口
│   ├── service/          # 服务层
│   ├── security/         # 安全相关
│   ├── utils/            # 工具类
│   └── HyperDutyApplication.java  # 应用入口
├── src/main/resources/
│   ├── application.yml   # 配置文件
│   └── sql/              # SQL脚本
└── pom.xml               # Maven配置
```

### 前端项目结构
```
frontend/
├── src/
│   ├── api/             # API请求
│   ├── components/      # 公共组件
│   ├── layout/          # 布局组件
│   ├── router/          # 路由配置
│   ├── store/           # 状态管理
│   ├── utils/           # 工具类
│   ├── views/           # 页面组件
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── vite.config.js       # Vite配置
└── package.json         # 依赖配置
```

## API设计规范

### 接口命名规范
- **GET**：查询资源，如 `/role/list` 获取角色列表
- **POST**：创建资源，如 `/role` 创建角色
- **PUT**：更新资源，如 `/role` 更新角色
- **DELETE**：删除资源，如 `/role/{id}` 删除角色

### 响应格式规范
```json
{
  "code": 200,               // 响应码
  "message": "操作成功",     // 响应消息
  "data": {...}             // 响应数据
}
```

### 错误码规范
- **200**：成功
- **400**：请求参数错误
- **401**：未认证
- **403**：禁止访问
- **404**：资源不存在
- **500**：服务器内部错误

## 开发规范

### 代码规范
- **命名规范**：
  - 类名：PascalCase
  - 方法名：camelCase
  - 变量名：camelCase
  - 常量名：UPPER_CASE
- **注释规范**：
  - 类、方法添加Javadoc注释
  - 复杂逻辑添加单行注释
- **代码风格**：遵循阿里巴巴Java开发规范

### 安全规范
- **密码安全**：使用BCrypt加密存储密码
- **SQL注入防护**：使用MyBatis Plus的参数绑定，避免直接拼接SQL
- **XSS防护**：对用户输入进行转义处理
- **CSRF防护**：采用JWT认证，避免CSRF攻击
- **API访问控制**：所有API添加适当的权限控制

## 数据库设计

### 核心数据表

- **用户表（sys_user）**：存储用户信息
- **角色表（sys_role）**：存储角色信息
- **菜单表（sys_menu）**：存储菜单信息
- **部门表（sys_dept）**：存储部门信息
- **员工表（sys_employee）**：存储员工信息
- **角色菜单关联表（sys_role_menu）**：存储角色与菜单的关联关系
- **角色用户关联表（sys_user_role）**：存储角色与用户的关联关系

## 部署说明

### 后端部署
1. **构建JAR包**：执行 `mvn clean package -DskipTests`
2. **运行JAR包**：执行 `java -jar target/hyper-duty-1.0.0.jar`

### 前端部署
1. **构建生产版本**：执行 `npm run build`
2. **Nginx配置**：参考标准Vue项目部署配置

## 版本控制

- **Git分支规范**：
  - `master`：主分支，用于生产环境
  - `develop`：开发分支，用于集成测试
  - `feature/*`：特性分支，用于开发新功能
  - `bugfix/*`：bug修复分支

- **提交信息规范**：
  - 格式：`类型: 描述`
  - 类型：`feat`(新功能), `fix`(修复bug), `docs`(文档), `style`(代码风格), `refactor`(重构), `test`(测试), `chore`(构建)
  - 示例：`feat: 添加角色菜单授权功能`