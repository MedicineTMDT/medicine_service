# ==========================================
# GIAI ĐOẠN 1: BUILD CODE (Dùng ảnh Maven mới hỗ trợ Java 21)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy toàn bộ thư mục dự án vào Docker
COPY . .

# Chạy lệnh build toàn bộ project
RUN mvn clean package -DskipTests

# ==========================================
# GIAI ĐOẠN 2: CHẠY APP (Dùng ảnh Java 21 siêu nhẹ)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Lấy file .jar đã được tạo ra từ thư mục target của module identity
COPY --from=builder /app/identity/target/*.jar app.jar

# Khai báo port ứng dụng đang chạy
EXPOSE 8080

# Lệnh để khởi động ứng dụng
CMD ["java", "-jar", "app.jar"]