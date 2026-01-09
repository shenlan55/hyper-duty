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
