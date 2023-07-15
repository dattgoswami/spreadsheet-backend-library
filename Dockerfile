# Start with a base image containing Java runtime
FROM maven:3.8-openjdk-11-slim as build

# Make the project folder the working directory
WORKDIR /app

# Copy the pom.xml file to download dependencies
COPY pom.xml .

# Download dependencies as specified in pom.xml
RUN mvn dependency:go-offline -B

# Copy the rest of the project into the working directory
COPY src /app/src

# Build the project
RUN mvn clean package -DskipTests

# Set the start-up command
CMD ["mvn", "test"]
