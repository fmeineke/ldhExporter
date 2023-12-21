FROM amazoncorretto:17
RUN mkdir /app
COPY target/SeekExporter-1.0.jar /app/java-application.jar
COPY target/lib /app/lib
WORKDIR /app
CMD "java" "-jar" "java-application.jar"
