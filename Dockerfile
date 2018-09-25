FROM kbase/kb_jre:latest

# These ARGs values are passed in via the docker build command
ARG BUILD_DATE
ARG VCS_REF
ARG BRANCH=develop

RUN apt-get -y update && apt-get -y install ant git openjdk-8-jdk make g++ libz-dev

COPY deployment/ /kb/deployment/
COPY jettybase/ /kb/deployment/jettybase/

# The BUILD_DATE value seem to bust the docker cache when the timestamp changes, move to
# the end
LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.vcs-url="https://github.com/kbaseIncubator/GeneHomologyPrototype.git" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.schema-version="1.0.0-rc1" \
      us.kbase.vcs-branch=$BRANCH \
      maintainer="Steve Chan sychan@lbl.gov"

WORKDIR /kb/deployment/jettybase
ENV GENE_HOMOLOGY_CONFIG=/kb/deployment/conf/deployment.cfg

RUN cd /opt \
  && wget -O last-956.zip https://www.dropbox.com/s/mj528m2ylp0nty5/last-956.zip?dl=1 \
  && unzip last-956.zip \
  && cd last-956 \
  && make

ENV PATH=/bin:/usr/bin:/kb/deployment/bin:$PWD/src

RUN chmod -R a+rwx /kb/deployment/conf /kb/deployment/jettybase/

ENTRYPOINT [ "/kb/deployment/bin/dockerize" ]

# Here are some default params passed to dockerize. They would typically
# be overidden by docker-compose at startup
CMD [  "-template", "/kb/deployment/conf/.templates/deployment.cfg.templ:/kb/deployment/conf/deployment.cfg", \
       "java", "-Djetty.home=/usr/local/jetty", "-jar", "/usr/local/jetty/start.jar" ]
