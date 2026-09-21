FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --chown=app:app target/banco-facil-api-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
