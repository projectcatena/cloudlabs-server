FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Set Environment Variables
ARG GOOGLE_APPLICATION_CREDENTIALS

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Clean up file name (see: https://stackoverflow.com/questions/61226664/build-docker-error-bin-sh-1-mvnw-not-found)
RUN sed -i 's/\r$//' ./mvnw
# Set executable permission
RUN chmod +x ./mvnw
# RUN ./mvnw install -DskipTests
RUN ./mvnw install
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.cloudlabs.server.ServerApplication"]