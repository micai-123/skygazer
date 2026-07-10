# 运行阶段
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/weather-backend-1.0.0.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
