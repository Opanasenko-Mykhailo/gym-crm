FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY gca-core-service gca-core-service

RUN mvn -f gca-core-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/gca-core-service/target/gca-core-service-*.jar /app/gca-core-service.jar

CMD ["java", "-jar", "/app/gca-core-service.jar"]