FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/doclus.jar /doclus/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/doclus/app.jar"]
