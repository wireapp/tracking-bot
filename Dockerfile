FROM docker.io/maven AS build-env

WORKDIR /app

COPY pom.xml ./

RUN mvn verify --fail-never -U

COPY . ./

RUN mvn -Dmaven.test.skip=true package

FROM dejankovacevic/bots.runtime:2.10.3

COPY --from=build-env /app/target/tracking.jar /opt/tracking/

COPY tracking.yaml         /etc/tracking/

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/opt/tracking/release.txt
RUN echo $release_version > /opt/tracking/release.txt

WORKDIR /opt/tracking

EXPOSE  8080 8081

ENTRYPOINT ["java", "-jar", "tracking.jar", "server", "/etc/tracking/tracking.yaml"]

