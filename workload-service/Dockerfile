FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml ./pom.xml
COPY workload-service workload-service

RUN mvn -f workload-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/workload-service/target/workload-service-*.jar /app/workload-service.jar

CMD ["java", "-jar", "/app/workload-service.jar"]