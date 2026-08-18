# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the fat JAR
RUN mvn clean package -DskipTests


# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the fat JAR created by maven-shade-plugin
COPY --from=builder /app/target/backend-1.0.0-SNAPSHOT-fat.jar app.jar

# Vert.x HTTP server
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "app.jar"]