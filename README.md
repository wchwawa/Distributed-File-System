# 高可用分布式KV存储系统
这个分布式Key-Value存储系统通过实现Raft一致性算法、使用一致性哈希进行数据分片、以及多种读性能优化技术，确保了系统在高负载下的高可用性和强一致性。
系统的设计目标不仅是满足高吞吐量和低延迟的性能需求，还通过多种机制提升了系统在面对复杂分布式环境时的鲁棒性和稳定性。
参考自[Raft论文](https://github.com/maemual/raft-zh_cn)和Raft作者的开源实现[LogCabin](https://github.com/logcabin/logcabin)。

这个分布式Key-Value存储系统的设计目标是确保高可用性和强一致性，同时能够在高负载下保持稳定的性能。具体来说，该系统的性能目标是在压力测试中能够达到每秒20,000次的查询处理能力（QPS），每个键值对的大小为4KB，同时确保99%的请求延迟（P99延迟）不超过800毫秒。
为了实现这个目标，系统采用了Raft一致性算法，这是一种用于管理分布式系统中日志复制的共识算法。Raft的核心功能包括领导者选举（Leader election）、日志复制（log replication）以及快照更新（snapshot update）。这些功能确保系统在出现节点故障或网络分区时，仍然能够保持一致性和可用性。

## Raft一致性算法的实现

	1.	领导者选举：Raft算法通过选举一个领导者节点（Leader）来管理日志的写入。领导者是唯一有权向其他节点（Follower）传播日志条目的节点。在系统启动或领导者失效时，Raft会发起选举过程，确保集群中总是有一个有效的领导者。
	2.	日志复制：一旦领导者被选举出来，它会将客户端的写请求转换为日志条目，并将这些条目复制到其他节点上。只有当大多数节点（通常是N/2+1个节点）确认接收到日志条目后，领导者才会将该条目应用到状态机中，并向客户端返回成功响应。
	3.	快照更新：为了防止日志无限增长，Raft支持创建快照的功能。快照是系统当前状态的一个压缩版本，保存了所有日志条目的结果。通过定期创建快照，系统可以清除过时的日志条目，节省存储空间并加速新节点的同步过程。

## 数据分片和一致性哈希

为了处理大规模的数据存储，系统采用了一致性哈希（consistent hashing）来进行数据分片（Sharding）。一致性哈希是一种将数据均匀分布到多个节点上的方法，避免了数据倾斜问题。在这种架构下，数据被分成多个分片（Shards），每个分片都由一个或多个Raft组（Raft Groups）管理。

	•	分片迁移：当系统扩展或缩减时，可能需要将某些分片的数据迁移到其他节点。这种迁移操作通过在多个Raft组之间进行协调来完成，以确保在迁移过程中数据的一致性和可用性。

## 读性能优化

系统在读性能优化方面做了以下几个改进：

	1.	异步应用（Asynchronous Apply）：通过异步应用日志条目，系统可以减少领导者处理客户端请求的延迟。这意味着领导者可以在日志条目被大多数Follower确认后立即返回结果，而不是等待这些条目被应用到状态机中。
	2.	读索引（ReadIndex）和FollowerRead：Raft通常要求所有读操作都经过领导者来保证一致性。然而，为了提高读操作的性能，系统可以使用ReadIndex技术来验证读请求是否可以在Follower上安全执行。如果某个Follower持有最新的日志条目并且能够确认它已被领导者应用，那么这个Follower就可以直接处理读请求，从而降低领导者的负载并减少读操作的延迟。
	3.	Prevote机制：Prevote是一种优化措施，用于减少不必要的领导者切换。当一个节点想要发起领导者选举时，它首先会发送Prevote请求，询问其他节点是否支持它成为领导者。如果大多数节点都表示支持，节点才会正式发起选举。这种机制有效地减少了由于网络分区或暂时性故障导致的频繁领导者切换，提高了系统的稳定性。

# 支持的功能
* leader选举
* 日志复制
* snapshot
* 集群成员动态更变

## Quick Start
在本地单机上部署一套3实例的raft集群，执行如下脚本：<br>
cd raft-java-example && sh deploy.sh <br>
该脚本会在raft-java-example/env目录部署三个实例example1、example2、example3；<br>
同时会创建一个client目录，用于测试raft集群读写功能。<br>
部署成功后，测试写操作，通过如下脚本：
cd env/client <br>
./bin/run_client.sh "list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" hello world <br>
测试读操作命令：<br>
./bin/run_client.sh "list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" hello

# 使用方法
下面介绍如何在代码中使用raft-java依赖库来实现一套分布式存储系统。
## 配置依赖（暂未发布到maven中央仓库，需要手动install到本地）
```
<dependency>
    <groupId>com.github.raftimpl.raft</groupId>
    <artifactId>raft-java-core</artifactId>
    <version>1.9.0</version>
</dependency>
```

## 定义数据写入和读取接口
```protobuf
message SetRequest {
    string key = 1;
    string value = 2;
}
message SetResponse {
    bool success = 1;
}
message GetRequest {
    string key = 1;
}
message GetResponse {
    string value = 1;
}
```
```java
public interface ExampleService {
    Example.SetResponse set(Example.SetRequest request);
    Example.GetResponse get(Example.GetRequest request);
}
```

## 服务端使用方法
1. 实现状态机StateMachine接口实现类
```java
// 该接口三个方法主要是给Raft内部调用
public interface StateMachine {
    /**
     * 对状态机中数据进行snapshot，每个节点本地定时调用
     * @param snapshotDir snapshot数据输出目录
     */
    void writeSnapshot(String snapshotDir);
    /**
     * 读取snapshot到状态机，节点启动时调用
     * @param snapshotDir snapshot数据目录
     */
    void readSnapshot(String snapshotDir);
    /**
     * 将数据应用到状态机
     * @param dataBytes 数据二进制
     */
    void apply(byte[] dataBytes);
}
```

2. 实现数据写入和读取接口
```
// ExampleService实现类中需要包含以下成员
private RaftNode raftNode;
private ExampleStateMachine stateMachine;
```
```
// 数据写入主要逻辑
byte[] data = request.toByteArray();
// 数据同步写入raft集群
boolean success = raftNode.replicate(data, Raft.EntryType.ENTRY_TYPE_DATA);
Example.SetResponse response = Example.SetResponse.newBuilder().setSuccess(success).build();
```
```
// 数据读取主要逻辑，由具体应用状态机实现
Example.GetResponse response = stateMachine.get(request);
```

3. 服务端启动逻辑

## 初始化RPCServer
RPCServer server = new RPCServer(localServer.getEndPoint().getPort());
// 应用状态机
ExampleStateMachine stateMachine = new ExampleStateMachine();
// 设置Raft选项，比如：
RaftOptions.snapshotMinLogSize = 10 * 1024;
RaftOptions.snapshotPeriodSeconds = 30;
RaftOptions.maxSegmentFileSize = 1024 * 1024;
// 初始化RaftNode
RaftNode raftNode = new RaftNode(serverList, localServer, stateMachine);
// 注册Raft节点之间相互调用的服务
RaftConsensusService raftConsensusService = new RaftConsensusServiceImpl(raftNode);
server.registerService(raftConsensusService);
// 注册给Client调用的Raft服务
RaftClientService raftClientService = new RaftClientServiceImpl(raftNode);
server.registerService(raftClientService);
// 注册应用自己提供的服务
ExampleService exampleService = new ExampleServiceImpl(raftNode, stateMachine);
server.registerService(exampleService);
// 启动RPCServer，初始化Raft节点
server.start();
raftNode.init();

