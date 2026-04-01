FROM ubuntu:latest
LABEL authors="s8gam"

ENTRYPOINT ["top", "-b"]




FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

CMD ["sh", "-c", "java -jar target/*.jar"]