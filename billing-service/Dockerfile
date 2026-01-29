FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY gradlew build.gradle settings.gradle /app/
COPY gradle /app/gradle
RUN chmod +x /app/gradlew
RUN /app/gradlew --no-daemon dependencies

COPY src /app/src
RUN /app/gradlew --no-daemon bootJar -x test
RUN cp /app/build/libs/*SNAPSHOT.jar /app/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/app.jar /app/app.jar
EXPOSE 8083
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
