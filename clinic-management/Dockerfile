# Stage 1: Build file .jar với Maven và Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copy file cấu hình và toàn bộ source code vào container
COPY pom.xml .
COPY src ./src
# Build ứng dụng, bỏ qua test để tăng tốc độ deploy
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng với môi trường Java 21 JRE Jammy (nhẹ và ổn định)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy file .jar đã build từ Stage 1 sang Stage 2
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Chạy ứng dụng kèm các tham số JVM bắt buộc từ file pom.xml
ENTRYPOINT ["java", \
            "--add-opens", "java.base/java.nio=ALL-UNNAMED", \
            "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", \
            "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
            "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED", \
            "-jar", "app.jar"]