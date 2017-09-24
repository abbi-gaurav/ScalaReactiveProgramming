FROM openjdk:8-jre-alpine

RUN addgroup akka-cluster && adduser -D -u 1001 -G akka-cluster akka-cluster
USER akka-cluster

WORKDIR /app
COPY target/scala-2.12/*assembly*.jar /app/

EXPOSE 2551
