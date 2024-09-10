#!/bin/bash

sh ./run_cluster.sh
nohup sh ./run_web.sh > /dev/null 2>&1 &
