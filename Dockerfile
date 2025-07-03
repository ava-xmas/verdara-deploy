# Use OpenJDK base image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy everything and build
COPY . .
RUN ./mvnw clean package

# Expose port
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "target/verdara-0.0.1-SNAPSHOT.jar"]
