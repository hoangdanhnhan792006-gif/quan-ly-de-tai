FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B -DskipTests package
COPY deploy/aiven-ca.pem /tmp/aiven-ca.pem
# Giữ các CA mặc định của Java và bổ sung CA công khai của Aiven.
RUN keytool -importcert -noprompt -trustcacerts -alias aiven-mysql \
    -file /tmp/aiven-ca.pem -cacerts -storepass changeit \
    && cp "$JAVA_HOME/lib/security/cacerts" /app/cacerts

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app && mkdir /app/uploads && chown app:app /app/uploads
COPY --from=build /app/target/quan-ly-de-tai-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/cacerts /app/certs/cacerts
USER app
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65", "-Djavax.net.ssl.trustStore=/app/certs/cacerts", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "app.jar"]
