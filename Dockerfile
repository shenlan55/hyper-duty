# 第一阶段：构建后端项目
FROM maven:3.8.5-openjdk-17 as build

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件
COPY pom.xml ./

# 复制src目录
COPY src ./src

# 构建项目，生成JAR包
RUN mvn clean package -DskipTests

# 第二阶段：使用Java基础镜像运行后端服务
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制构建产物到工作目录
COPY --from=build /app/target/hyper-duty-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 环境变量由docker-compose.yml提供

# 启动应用
CMD ["java", "-jar", "app.jar"]