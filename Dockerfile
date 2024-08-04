# 使用官方 OpenJDK 17 镜像作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 WAR 文件到工作目录
COPY ty-multiverse-backend.war /app/ty-multiverse-backend.war

# 暴露应用程序运行的端口
EXPOSE 8080

# 启动应用程序
ENTRYPOINT ["java", "-jar", "ty-multiverse-backend.war"]
