#!/usr/bin/env bash

cd ../../raft-java-example/env

for i in 1 2 3
do
  cd example$i
  nohup ./bin/run_server.sh ./data "127.0.0.1:8051:1,127.0.0.1:8052:2,127.0.0.1:8053:3" "127.0.0.1:805$i:$i" "$i" > /dev/null 2>&1 &
  cd -
done

cd ../../web-client/bin
