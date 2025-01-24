# Start with a base image that has Java (using Eclipse Temurin for JDK 17)
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy the application's JAR and dependencies
COPY target/ty-multiverse-backend.jar /app/ty-multiverse-backend.jar

# Set the working directory
WORKDIR /app

# Expose the application's port
EXPOSE 8080

# Set the environment variable for Spring profile
ENV SPRING_PROFILES_ACTIVE=platform

# Run the application with the specified profile
ENTRYPOINT ["java", "-jar", "/app/ty-multiverse-backend.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
