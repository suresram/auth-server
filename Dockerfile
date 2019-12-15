FROM maven:3.6-jdk-8 as maven
WORKDIR /app
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src

RUN mvn package && cp target/auth-*.jar app.jar

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=maven /app/app.jar ./app.jar

EXPOSE 8764

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.cloud.config.uri=http://config-server-outreach.apps.na311.openshift.opentlc.com","-jar","/app/app.jar"]