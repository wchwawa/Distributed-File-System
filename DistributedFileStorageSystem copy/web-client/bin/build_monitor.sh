#!/bin/bash

# 准备Docker环境
yum -y install gcc
yum -y install gcc-c++
yum -y install yum-utils
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
yum makecache fast
# 安装Docker
yum -y install docker-ce docker-ce-cli containerd.io
systemctl start docker
docker version
systemctl enable docker
# 拉取镜像
docker pull prom/prometheus
docker pull grafana/grafana
# 创建Prometheus容器
docker create -p 9090:9090 --privileged=true \
-v /usr/local/raft/web-client/bin/conf/prometheus.yml:/etc/prometheus/prometheus.yml \
--name=prometheus --restart=unless-stopped \
prom/prometheus:latest
# 创建Grafana容器
docker create -p 3000:3000 --privileged=true \
--name=grafana --restart=unless-stopped \
grafana/grafana:latest
