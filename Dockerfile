FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
# Descarga dependencias sin compilar/ejecutar tests
RUN mvn -B -Dmaven.test.skip=true dependency:go-offline

COPY src ./src

# Permite decidir si se ejecutan tests durante el build (por defecto true para CI)
ARG RUN_TESTS=true

# Ejecuta pruebas si RUN_TESTS=true (fallará el build si algo rompe)
RUN if [ "$RUN_TESTS" = "true" ]; then \
            mvn -B test jacoco:report; \
        else \
            echo "RUN_TESTS=false: omitiendo ejecución de tests durante el build"; \
        fi

# Empaqueta sin volver a ejecutar tests (se omitirá también si RUN_TESTS=false)
RUN mvn -B -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --create-home --shell /bin/bash appuser \
    && mkdir -p /app/logs \
    && chown -R appuser:appuser /app
USER appuser

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
