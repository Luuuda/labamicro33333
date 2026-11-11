FROM maven:3.9.6-eclipse-temurin-22-jammy AS build
WORKDIR /workspace
COPY pom.xml .
COPY app/pom.xml app/
COPY catalog/pom.xml catalog/
COPY config-server/pom.xml config-server/
COPY gateway/pom.xml gateway/
COPY reviews/pom.xml reviews/
COPY users/pom.xml users/

COPY app/src app/src
COPY catalog/src catalog/src
COPY config-server/src config-server/src
COPY gateway/src gateway/src
COPY reviews/src reviews/src
COPY users/src users/src

RUN mvn -pl app clean package -DskipTests -am

FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
