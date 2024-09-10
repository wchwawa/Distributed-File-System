#!/bin/bash

nohup jstatd -J-Djava.security.policy=${JAVA_HOME}/jre/lib/security/jstatd.all.policy \
-J-Djava.rmi.server.logCalls=true \
-p 18080 -J-Djava.rmi.server.hostname=192.168.227.117 > /dev/null 2>&1 &
