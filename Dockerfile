FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV JAVA_OPTS=""
ENV SERVER_PORT=8080

COPY --from=build /workspace/target/*.jar /app/qilu.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/qilu.jar"]
