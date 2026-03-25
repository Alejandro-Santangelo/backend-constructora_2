# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar archivos de configuración Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Ejecutar la aplicación con límites de memoria y perfil prod
# Railway plan: Ajustar según plan contratado
# 1GB RAM → usar -Xmx512m
# 2GB RAM → usar -Xmx1024m  
# 4GB RAM → usar -Xmx2048m
ENTRYPOINT ["java", \
    "-Xmx512m", \
    "-Xms256m", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dspring.profiles.active=prod", \
    "-jar", "app.jar"]
