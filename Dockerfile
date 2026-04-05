FROM openjdk:17-slim

WORKDIR /app

COPY *.java .

RUN javac *.java

EXPOSE 5000

CMD ["java", "MainServer"]
