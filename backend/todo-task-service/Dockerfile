FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./
COPY todo-eventsourcing-core/pom.xml todo-eventsourcing-core/
COPY todo-task-service/pom.xml todo-task-service/
COPY todo-oauth-mock-server/pom.xml todo-oauth-mock-server/
RUN ./mvnw -B -q -pl todo-eventsourcing-core,todo-task-service -am dependency:go-offline -DskipTests
COPY todo-eventsourcing-core/src todo-eventsourcing-core/src
COPY todo-task-service/src todo-task-service/src
RUN ./mvnw -B -q -pl todo-eventsourcing-core,todo-task-service -am package -DskipTests

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
RUN useradd --uid 10001 --create-home service
COPY --from=build /build/todo-task-service/target/todo-task-service.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
