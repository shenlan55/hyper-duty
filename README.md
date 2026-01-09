### 启动步骤
1. 初始化数据库 ：执行 src/main/resources/sql/init.sql 创建数据库和表
2. 启动后端 ：
   ```
   mvn spring-boot:run
   # 或
   java -jar target/hyper-duty-1.0.0.jar
   ```
3. 启动前端 ：
   ```
   cd frontend
   npm run dev
   ```
4. 访问应用 ：
   - 前端： http://localhost:5173
   - 登录账号：admin/123456


技术栈 ：

- 后端：Spring Boot 3.1.0 + Spring Security + MyBatis Plus 3.5.6 + Druid
- 前端：Vue 3 + Vite 4.5.0 + Element Plus + Pinia + Vue Router