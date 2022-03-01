FROM ubuntu:20.04

RUN apt update

RUN apt install -y curl

RUN apt install -y openjdk-8-jdk

RUN mkdir /opt/tomcat/

WORKDIR /opt/tomcat
RUN curl -O https://downloads.apache.org/tomcat/tomcat-9/v9.0.59/bin/apache-tomcat-9.0.59.tar.gz
RUN tar xvfz apache*.tar.gz
RUN mv apache-tomcat-9.0.59/* /opt/tomcat/.

EXPOSE 8080

CMD ["/opt/tomcat/bin/catalina.sh", "run"]

# ENTRYPOINT ["/audit-control-environment/docker/devenv/deventry.sh"]
