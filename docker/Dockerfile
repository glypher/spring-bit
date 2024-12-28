FROM eclipse-temurin:23-jre AS builder
WORKDIR /app
ARG ARTIFACT_NAME
COPY ${ARTIFACT_NAME}.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher


FROM eclipse-temurin:23
WORKDIR /app

ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}

ENV SPRING_PROFILES_ACTIVE=production

COPY --from=builder /app/application/dependencies/ ./

RUN true
COPY --from=builder /app/application/spring-boot-loader/ ./
RUN true
COPY --from=builder /app/application/snapshot-dependencies/ ./
RUN true
COPY --from=builder /app/application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
