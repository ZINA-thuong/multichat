# Sử dụng Java 21 vì pom.xml của em quy định version 21
FROM eclipse-temurin:21-jre 

# Copy file jar từ máy thật vào trong container
COPY target/projectcloud-1.0-SNAPSHOT.jar app.jar

# Mở port 8888 để các máy khác có thể kết nối vào Server
EXPOSE 8888

# Lệnh để chạy ứng dụng
#ENTRYPOINT ["java", "-jar", "app.jar"]