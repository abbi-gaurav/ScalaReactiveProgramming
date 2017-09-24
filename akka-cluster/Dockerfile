FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/scala-2.12/*assembly*.jar /app/
EXPOSE 2151
ENTRYPOINT exec java -jar *assembly*.jar
