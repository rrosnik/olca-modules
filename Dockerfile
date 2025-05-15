# Stage 1: Build the Java project
FROM maven:3.9.6-eclipse-temurin-22-alpine AS build

# Set the working directory
WORKDIR /app

# Copy your Maven project files
COPY . .

# Download dependencies (cache layer)
RUN mvn dependency:go-offline

# Build all modules
RUN mvn clean package -DskipTests

# Stage 2: Create the final minimal image
FROM eclipse-temurin:22-jre-alpine

ENV NBLA_LCA_SERVICE_DB_NAME="ecoinvent"
ENV NBLA_LCA_SERVICE_PORT=4022
ENV NBLA_LCA_SERVICE_THREADS=4

# Set the working directory
WORKDIR /app/lib

# Copy the built jar from the builder
COPY --from=build /app/target/*.jar .
COPY --from=build /app/target/dist/lib/*.jar .

COPY --from=build /app/olca-ipc/target/*.jar .
COPY --from=build /app/olca-ipc/target/dist/lib/*.jar .

COPY --from=build /app/olca-core/target/*.jar .
COPY --from=build /app/olca-core/target/dist/lib/*.jar .

# Expose the port your app uses (optional)
EXPOSE 8080

# to run software we change directory to the app
WORKDIR /app

# run ipc server
#ENTRYPOINT ["java", "-Dderby.user.APP=app", "-cp", "./lib/*", "org.openlca.ipc.Server" ,"-data", "G:\Projects\project_1\programming\Activity_2", "-db", "ecoinvent", "-port" ,"4022", "-threads", "4"]
#ENTRYPOINT ["java", "-Dderby.user.APP=app", "-cp", "./lib/*", "org.openlca.ipc.Server" ,"-data", "/app/dbDir", "-db", "${NBLA_LCA_SERVICE_DB_NAME})", "-port" ,"${NBLA_LCA_SERVICE_PORT}", "-threads", "${NBLA_LCA_SERVICE_THREADS}"]
ENTRYPOINT sh -c 'java -Dderby.user.APP=app -cp "./lib/*" org.openlca.ipc.Server -data "/app/dbDir" -db "$NBLA_LCA_SERVICE_DB_NAME" -port "$NBLA_LCA_SERVICE_PORT" -threads "$NBLA_LCA_SERVICE_THREADS"'

