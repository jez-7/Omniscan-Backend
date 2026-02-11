FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080
ADD target/monitoreo-*.jar omniscan.jar
ENTRYPOINT ["java", "-jar", "/omniscan.jar"]