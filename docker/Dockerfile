#FROM dockregxwr-on.azurecr.io/tomcatbase:latest
#FROM tomcat:9.0-jre8-alpine
FROM tomcat:8.5.42-jdk8-openjdk


ENV CATALINA_HOME /usr/local/tomcat
#ENV PATH $CATALINA_HOME/bin:$PATH

#Provision Webapp	
ADD ./SeicentoBilling.war $CATALINA_HOME/webapps/
ADD ./UrlProtocolExtender.jar /usr/lib/jvm/java-1.8-openjdk/jre/lib/ext
ADD ./UrlProtocolExtender.jar /usr/local/openjdk-8/jre/lib/ext

EXPOSE 8080
CMD ["catalina.sh", "run"]
