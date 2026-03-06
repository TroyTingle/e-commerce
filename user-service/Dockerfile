FROM eclipse-temurin:25-jre@sha256:0a9c973778b03b88f39ccae4f8cc26022d84a3237a818cb98770369eb6c5daf9

COPY target/user-service-*.jar /user-service.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/user-service.jar"]