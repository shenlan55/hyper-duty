# Hyper Duty 综合管理系统

## 项目概述

Hyper Duty System 是一款面向企业的综合管理系统，集成了系统管理、值班管理和项目管理等核心功能，为企业提供全面的管理解决方案。

## 技术栈

- **后端**：Spring Boot 3.1.0 + Spring Security + MyBatis Plus 3.5.6 + Druid 1.2.20 + PostgreSQL 18.2.1 + Redis 8.6.0
- **前端**：Vue 3.5.24 + Vite 4.5.0 + Element Plus 2.13.1 + Pinia 3.0.4 + Vue Router 4.6.4 + Axios 1.13.2
- **存储**：RustFS 1.0.0 (对象存储)

## 功能模块

### 1. 系统管理
- 员工信息管理：添加、修改、删除员工信息
- 部门管理：部门树形结构管理
- 角色管理：角色创建、编辑、删除
- 权限管理：基于角色的菜单权限控制
- 字典管理：系统字典配置

### 2. 值班管理
- 排班管理：创建、修改、删除排班计划
- 值班记录：记录值班情况，包括签到、签退、异常情况
- 值班统计：按时间段、员工、部门统计值班情况

### 3. 项目管理
- 项目管理：项目创建、编辑、删除、归档
- 任务管理：任务创建、编辑、删除，支持3级任务层级
- 任务进度：实时更新任务进度，自动计算项目进度
- 甘特图：可视化项目进度和任务时间线

### 4. 菜单管理
- 菜单树形结构管理
- 菜单权限分配：为不同角色分配不同菜单权限

### 5. 认证授权
- 用户登录：JWT令牌认证
- 权限控制：基于角色的访问控制

## 启动步骤

1. **初始化数据库**：执行 `src/main/resources/sql/hyper_duty_ddl_postgresql.sql` 创建数据库和表
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
   - 登录账号：admin/admin123

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

#### 系统管理模块
- **用户表（sys_user）**：存储用户信息
- **角色表（sys_role）**：存储角色信息
- **菜单表（sys_menu）**：存储菜单信息
- **部门表（sys_dept）**：存储部门信息
- **员工表（sys_employee）**：存储员工信息
- **角色菜单关联表（sys_role_menu）**：存储角色与菜单的关联关系
- **角色用户关联表（sys_user_role）**：存储角色与用户的关联关系
- **字典表（sys_dict）**：存储字典类型信息
- **字典数据表（sys_dict_data）**：存储字典数据信息

#### 值班管理模块
- **排班表（duty_schedule）**：存储排班计划信息
- **排班模式表（duty_schedule_mode）**：存储排班模式信息
- **值班分配表（duty_assignment）**：存储值班分配信息
- **值班记录表（duty_record）**：存储值班记录信息
- **班次配置表（duty_shift_config）**：存储班次配置信息
- **请假申请表（leave_request）**：存储请假申请信息
- **请假审批表（leave_approval）**：存储请假审批信息
- **调班申请表（swap_request）**：存储调班申请信息
- **调班审批表（swap_approval）**：存储调班审批信息
- **员工可用时间表（employee_available_time）**：存储员工可用时间信息
- **节假日表（duty_holiday）**：存储节假日信息

#### 项目管理模块
- **项目表（pm_project）**：存储项目基本信息
- **项目成员表（pm_project_member）**：存储项目成员信息
- **任务表（pm_task）**：存储任务信息
- **任务关联人表（pm_task_stakeholder）**：存储任务关联人信息
- **任务附件表（pm_task_attachment）**：存储任务附件信息
- **任务进度表（pm_task_progress）**：存储任务进度记录
- **任务评论表（pm_task_comment）**：存储任务评论信息
- **任务更新申请表（pm_task_update_request）**：存储任务更新申请信息
- **项目附件表（pm_project_attachment）**：存储项目附件信息

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