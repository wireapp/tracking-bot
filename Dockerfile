FROM maven:3.6.3-jdk-8-slim AS build-env

WORKDIR /app

# download dependencies
COPY pom.xml ./
RUN mvn verify --fail-never -U

# build
COPY . ./
RUN mvn -Dmaven.test.skip=true package

# runtime stage
FROM wirebot/runtime
RUN mkdir /etc/tracking /opt/tracking

COPY --from=build-env /app/target/tracking.jar /opt/tracking/

COPY tracking.yaml /etc/tracking/

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/opt/tracking/release.txt
RUN echo $release_version > /opt/tracking/release.txt

WORKDIR /opt/tracking

EXPOSE  8080 8081 8082
ENTRYPOINT ["java", "-javaagent:/opt/wire/lib/prometheus-agent.jar=8082:/opt/wire/lib/metrics.yaml", "-jar", "tracking.jar", "server", "/etc/tracking/tracking.yaml"]
