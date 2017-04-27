FROM tomcat:7
ENV CATALINA_HOME="/usr/local/tomcat"
ENV CATALINA_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m"
ENV JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx1024m"
COPY smartbank-services.war  $CATALINA_HOME/webapps/
COPY tomcat-users.xml $CATALINA_HOME/conf/tomcat-users.xml
EXPOSE 8888:8080
CMD ["catalina.sh", "run"]
