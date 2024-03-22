# Use a Maven image to build the project
FROM maven:3.8.4-openjdk-17-slim AS builder

# Set the working directory
WORKDIR /app

# Copy the Maven project descriptor
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean install -DskipTests=true

# Use the OpenJDK image as the final image
FROM openjdk:17

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/kubernetes-0.0.1-SNAPSHOT.jar app.jar

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]