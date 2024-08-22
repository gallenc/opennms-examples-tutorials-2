# injects new logos 

FROM opennms/horizon:32.0.5

USER root
    
# inject new logo for rebrand 

COPY opt/opennms/jetty-webapps/opennms/images/opennms-horizon-logo.svg  /usr/share/opennms/jetty-webapps/opennms/images/opennms-horizon-logo.svg

COPY opt/opennms/jetty-webapps/opennms/images/opennms-horizon-logo.png  /usr/share/opennms/jetty-webapps/opennms/images/opennms-horizon-logo.png
    
USER 10001