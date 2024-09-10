#!/bin/bash

java -jar /usr/local/AlibabaArthas/arthas-boot.jar \
--tunnel-server 'ws://192.168.2.118:7777/ws' \
--target-ip '192.168.227.117' --http-port 7777 \
--attach-only $1
