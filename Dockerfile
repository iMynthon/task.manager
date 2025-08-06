FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY build/libs/task.manager-0.0.1-SNAPSHOT-boot.jar app.jar
ENV TASK_PORT=8080
EXPOSE ${TASK_PORT}
ENTRYPOINT ["java","-jar","app.jar"]