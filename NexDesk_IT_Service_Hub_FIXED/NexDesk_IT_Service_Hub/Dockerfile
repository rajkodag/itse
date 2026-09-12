FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY lib/ojdbc11.jar /app/lib/ojdbc11.jar
COPY src /app/src
COPY web /app/web

RUN mkdir -p /app/classes

RUN javac -cp "/app/lib/ojdbc11.jar" \
    -d /app/classes \
    $(find /app/src -name "*.java")

RUN cp -r /app/web /app/classes/web

EXPOSE 8080

CMD ["java", "-cp", "/app/classes:/app/lib/ojdbc11.jar", "com.nexdesk.NexDeskApplication"]