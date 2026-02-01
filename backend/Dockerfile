FROM openjdk:25-ea

WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

RUN cp target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
