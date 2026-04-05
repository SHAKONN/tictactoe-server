FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY *.java .

RUN javac *.java

EXPOSE 5000

CMD ["java", "MainServer"]
