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
RUN mkdir -p /srv/gca/figures
RUN mkdir -p /srv/gca/figures_mobile

ADD app /srv/gca/app
ADD conf /srv/gca/conf
ADD project/plugins.sbt /srv/gca/project/
ADD project/build.properties /srv/gca/project/
ADD public /srv/gca/public
ADD test /srv/gca/test
ADD build.sbt /srv/gca/

# only required for local tests
RUN mkdir -p /srv/gca/db
RUN echo "db.default.url=\"jdbc:h2:/srv/gca/db/gca-web\"" >> /srv/gca/conf/application.dev.conf

# test and stage
WORKDIR /srv/gca
# Required to get dependencies before running the startup script.
RUN activator test stage

VOLUME ["/srv/gca/db"]
VOLUME ["/srv/gca/figures"]
VOLUME ["/srv/gca/figures_mobile"]

# Add an external directory for production config files.
RUN mkdir -p /srv/ext_conf
VOLUME ["/srv/ext_conf"]

EXPOSE 9000
ADD docker_startup.sh /srv/gca
RUN chmod +x ./docker_startup.sh
ENTRYPOINT ["./docker_startup.sh"]
