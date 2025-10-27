FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY gateway-service gateway-service

RUN mvn -f gateway-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/gateway-service/target/gateway-service-*.jar /app/gateway-service.jar

CMD ["java", "-jar", "/app/gateway-service.jar"]