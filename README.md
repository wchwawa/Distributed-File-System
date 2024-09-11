# 🚀 High-Performance Distributed KV Storage System

‼️ **Must Use x86-Linux**

For Mac M-chip users, it’s recommended to use an x86 Linux Docker image. Performance may slightly decline, but it’s easy to set up.

## 📌 System Overview

1. This project simulates a 3-node Raft cluster by replicating its core functions, such as leader election, log replication, snapshot updates, and dynamic cluster membership changes.
2. The project integrates RocksDB as the state machine for the Raft cluster. The state machine serves as the heart of the system. For example, in a write operation, if a majority of nodes agree, the RocksDB state machine will be the final place to apply and store the key-value pairs. RocksDB also handles snapshots to help failed nodes recover consistent data.
3. Spring Boot is used to create read and write endpoints for benchmarking.

In the example service, three write logics are implemented: eventual consistency, strong consistency, and non-strong consistency.

This implementation is based on the [Raft paper](https://raft.github.io/) and the open-source project by Raft’s author, [LogCabin](https://github.com/logcabin/logcabin).

## 🎯 Performance Goals

- Handle 20,000 queries per second (QPS)
- Key-value pair size: 4KB
- P99 latency: under 800ms

## 🛠 Core Technologies

### 1. Raft Consensus Algorithm 🔄

#### 1.1 Leader Election
Ensures the cluster always has a valid leader node.

#### 1.2 Log Replication
The leader manages log writes and replicates log entries to other nodes.

#### 1.3 Snapshot Updates
Periodically creates snapshots of the system state to optimize storage and speed up new node synchronization.

### 2. Sharding and Consistent Hashing 🧩
- Uses consistent hashing to evenly distribute data.
- Supports shard migration to ensure data consistency and availability.

### 3. Read Performance Optimization 🚀

#### 3.1 Asynchronous Apply
Reduces leader processing latency for client requests.

#### 3.2 ReadIndex and FollowerRead
Allows safe read operations on Follower nodes, reducing the leader’s load.

#### 3.3 Prevote Mechanism
Reduces unnecessary leader switches, improving system stability.

## 💪 Supported Features
- ✅ Leader Election
- ✅ Log Replication
- ✅ Snapshot
- ✅ Dynamic Cluster Membership Changes

## 🚀 Quick Start

### 1. Test and Deploy Raft Cluster
Deploy a 3-instance Raft cluster on a single local machine by running the `deploy.sh` script in the `example` subproject.
This script will set up three instances: `example1`, `example2`, and `example3` in the `raft-java-example/env` directory. It will also create a `client` directory to test the Raft cluster’s read and write functions.

After a successful deployment, test the write operation using the following commands:

1. Navigate to `env/client`
2. Run:
   ```bash
   ./bin/run_client.sh "list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" hello world

Test read operation with the following command:
   ``` bash
   ./bin/run_client.sh "list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" hello

Test Client Write and Read

Navigate to the web-client client and run the deploy.sh script.

For Write Operations:

Use Postman or curl to send a POST request to http://127.0.0.1:8080/raft/write with the request body: "key=xx&value=xx".

For Read Operations:

Use Postman or curl to send a GET request to http://127.0.0.1:8080/raft/read?key=xx.
