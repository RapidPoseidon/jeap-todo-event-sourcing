FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./
COPY todo-eventsourcing-core/pom.xml todo-eventsourcing-core/
COPY todo-task-service/pom.xml todo-task-service/
COPY todo-oauth-mock-server/pom.xml todo-oauth-mock-server/
COPY todo-oauth-mock-server/src todo-oauth-mock-server/src
RUN ./mvnw -B -q -pl todo-oauth-mock-server package -DskipTests

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
RUN useradd --uid 10001 --create-home service
COPY --from=build /build/todo-oauth-mock-server/target/todo-oauth-mock-server.jar app.jar
USER 10001
EXPOSE 8180
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
