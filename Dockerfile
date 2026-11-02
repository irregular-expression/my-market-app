FROM eclipse-temurin:21-jdk
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar", "--spring.datasource.url=jdbc:postgresql://host.docker.internal:5433/postgres?currentSchema=market"]