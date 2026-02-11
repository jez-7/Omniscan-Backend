FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080
ADD target/monitoreo-*.jar monitoreo-*.jar
ENTRYPOINT ["java", "-jar", "/monitoreo-*.jar"]