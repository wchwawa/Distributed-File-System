#!/bin/bash

cd ../target
RUNJAVA="$JAVA_HOME/bin/java"
JMX_OPT=" -Djava.rmi.server.hostname=192.168.227.117 \
-Dcom.sun.management.jmxremote.port=8081 \
-Dcom.sun.management.jmxremote.rmi.port=8081 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false "
JAR="web-client-1.0.0.jar"
$RUNJAVA -jar $JMX_OPT $JAR
cd -
