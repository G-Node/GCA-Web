# Dockerfile for GCA-Web
FROM java:8

# same as travis
ENV  ACTIVATOR_VERSION 1.3.7
ENV  DEBIAN_FRONTEND noninteractive

WORKDIR /tmp
RUN wget https://downloads.typesafe.com/typesafe-activator/${ACTIVATOR_VERSION}/typesafe-activator-${ACTIVATOR_VERSION}-minimal.zip
RUN unzip -q typesafe-activator-${ACTIVATOR_VERSION}-minimal.zip -d /usr/local

ENV PATH /usr/local/activator-${ACTIVATOR_VERSION}-minimal:$PATH

# install to srv gca
RUN mkdir -p /srv/gca

ADD app /srv/gca/app
ADD conf /srv/gca/conf
ADD project/plugins.sbt /srv/gca/project/
ADD project/build.properties /srv/gca/project/
ADD public /srv/gca/public
ADD test /srv/gca/test
ADD build.sbt /srv/gca/
ADD startup.sh /srv/gca
ADD figures /srv/gca/figures
ADD figures_mobile /srv/gca/figures_mobile

RUN mkdir -p /srv/gca/db
RUN echo "db.default.url=\"jdbc:h2:/srv/gca/db/gca-web\"" >> /srv/gca/conf/application.dev.conf

# test and stage
WORKDIR /srv/gca
# Required to get dependencies before running the startup script.
RUN activator test stage

# Make sure we always have the latest routes file available
# even if we use a config folder from outside the container.
RUN mkdir -p /srv/tmp
RUN cp /srv/gca/conf/routes /srv/tmp/routes

VOLUME ["/srv/gca/db"]
VOLUME ["/srv/gca/conf"]
VOLUME ["/srv/gca/figures"]
VOLUME ["/srv/gca/figures_mobile"]

EXPOSE 9000
ENTRYPOINT ["/bin/bash", "startup.sh"]

# Previous entrypoint using the staged binary
#ENTRYPOINT ["target/universal/stage/bin/gca-web"]
