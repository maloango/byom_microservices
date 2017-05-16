############################################################
# Dockerfile to build core-service
# Based on Tomcat
############################################################


# Set the base image to Tomcat
FROM tomcat:7

# File Author / Maintainer
MAINTAINER Serge MBikina

#Define the ENV variables
ENV CATALINA_HOME="/usr/local/tomcat"
ENV CATALINA_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m"
ENV JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx1024m"

#Copy web application configuration
COPY core-service.war  $CATALINA_HOME/webapps/
COPY tomcat-users.xml $CATALINA_HOME/conf/tomcat-users.xml

# Configure Services and Port
EXPOSE 8888:8080
CMD ["catalina.sh", "run"]

