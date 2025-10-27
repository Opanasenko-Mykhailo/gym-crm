FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY discovery-server discovery-server

RUN mvn -f discovery-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/discovery-server/target/discovery-server-*.jar /app/discovery-server.jar

CMD ["java", "-jar", "/app/discovery-server.jar"]