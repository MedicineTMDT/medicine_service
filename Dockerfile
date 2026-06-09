# ==========================================
# GIAI ĐOẠN 1: BUILD CODE
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy toàn bộ dự án
COPY . .

# Chỉ build module mono (để đảm bảo file jar nằm trong mono/target)
# -pl là project list, -am là also make (build các phụ thuộc nếu có)
RUN mvn clean package -DskipTests -pl mono -am

# ==========================================
# GIAI ĐOẠN 2: CHẠY APP
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Kiểm tra kỹ tên file JAR. 
# Nếu trong mono/target có nhiều file jar, lệnh *.jar có thể gây lỗi.
# Tốt nhất là trỏ đích danh hoặc dùng lệnh copy cẩn thận.
COPY --from=builder /app/mono/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]