FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080
ADD target/monitoreo.jar monitoreo-new.jar
ENTRYPOINT ["java", "-jar", "/monitoreo-new.jar"]