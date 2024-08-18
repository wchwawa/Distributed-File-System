FROM ubuntu:latest

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    wget \
    autoconf \
    automake \
    libtool \
    g++ \
    make \
    openjdk-8-jdk \
    maven

# 下载并安装 Protobuf 2.5.0

# 设置 JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

# 设置工作目录
WORKDIR /app

# 容器启动时运行的命令
CMD ["/bin/bash"]