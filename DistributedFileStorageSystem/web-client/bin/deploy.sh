#!/usr/bin/env bash

cd ../../raft-java-example
mvn clean install:install-file -Dfile=./lib/bitcask-java.jar -DgroupId=com.github.bitcask \
-DartifactId=bitcask-java -Dversion=0.0.1 -Dpackaging=jar -DskipTests
sh deploy.sh
mvn install -DskipTests
cd ../web-client
mvn clean package -DskipTests

cd ./bin
chmod +x ./*.sh
nohup ./run_web.sh > /dev/null 2>&1 &
