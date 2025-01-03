FROM amazoncorreto:21-alpine-jdk

COPY target/price-comparator-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]