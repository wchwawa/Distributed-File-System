package com.github.raftimpl.raft.example.server.service.impl;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.client.instance.Endpoint;
import com.github.raftimpl.raft.Peer;
import com.github.raftimpl.raft.RaftNode;
import com.github.raftimpl.raft.StateMachine;
import com.github.raftimpl.raft.example.server.service.ExampleProto;
import com.github.raftimpl.raft.example.server.service.ExampleService;
import com.github.raftimpl.raft.proto.RaftProto;
import com.googlecode.protobuf.format.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by raftimpl on 2017/5/9.
 */
public class ExampleServiceImpl implements ExampleService {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleServiceImpl.class);
    private static JsonFormat jsonFormat = new JsonFormat();

    private RaftNode raftNode;
    private StateMachine stateMachine;
    private int leaderId = -1;
    private RpcClient leaderRpcClient = null;
    private ExampleService leaderService = null;
    private Lock leaderLock = new ReentrantLock();

    public ExampleServiceImpl(RaftNode raftNode, StateMachine stateMachine) {
        this.raftNode = raftNode;
        this.stateMachine = stateMachine;
    }

    private void onLeaderChangeEvent() {
        if (raftNode.getLeaderId() != -1
                && raftNode.getLeaderId() != raftNode.getLocalServer().getServerId()
                && leaderId != raftNode.getLeaderId()) {
            leaderLock.lock();
            if (leaderId != -1 && leaderRpcClient != null) {
                leaderRpcClient.stop();
                leaderRpcClient = null;
                leaderId = -1;
            }
            leaderId = raftNode.getLeaderId();
            Peer peer = raftNode.getPeerMap().get(leaderId);
            Endpoint endpoint = new Endpoint(peer.getServer().getEndpoint().getHost(),
                    peer.getServer().getEndpoint().getPort());
            RpcClientOptions rpcClientOptions = new RpcClientOptions();
            rpcClientOptions.setGlobalThreadPoolSharing(true);
            leaderRpcClient = new RpcClient(endpoint, rpcClientOptions);
            leaderService = BrpcProxy.getProxy(leaderRpcClient, ExampleService.class);
            leaderLock.unlock();
        }
    }

    @Override
    public ExampleProto.SetResponse set(ExampleProto.SetRequest request) {
        ExampleProto.SetResponse.Builder responseBuilder = ExampleProto.SetResponse.newBuilder();
        // 如果自己不是leader，将写请求转发给leader
        if (raftNode.getLeaderId() <= 0) {
            responseBuilder.setSuccess(false);
        } else if (raftNode.getLeaderId() != raftNode.getLocalServer().getServerId()) {
            onLeaderChangeEvent();
            ExampleProto.SetResponse responseFromLeader = leaderService.set(request);
            responseBuilder.mergeFrom(responseFromLeader);
        } else {
            // 数据同步写入raft集群
            byte[] data = request.toByteArray();
            boolean success = raftNode.replicate(data, RaftProto.EntryType.ENTRY_TYPE_DATA);
            responseBuilder.setSuccess(success);
        }

        ExampleProto.SetResponse response = responseBuilder.build();
        LOG.info("set request, request={}, response={}", jsonFormat.printToString(request),
                jsonFormat.printToString(response));
        return response;
    }

    /*@Override
    public ExampleProto.GetResponse get(ExampleProto.GetRequest request) {
        // Follower-read 最终一致性
        ExampleProto.GetResponse.Builder responseBuilder = ExampleProto.GetResponse.newBuilder();
        byte[] keyBytes = request.getKey().getBytes();
        byte[] valueBytes = stateMachine.get(keyBytes);
        if (valueBytes != null) {
            String value = new String(valueBytes);
            responseBuilder.setValue(value);
        }
        ExampleProto.GetResponse response = responseBuilder.build();
        LOG.info("get request, request={}, response={}", jsonFormat.printToString(request),
                jsonFormat.printToString(response));
        return response;
    }*/

    @Override
    public ExampleProto.GetResponse get(ExampleProto.GetRequest request) {
        // Follower-read 非强一致性
        ExampleProto.GetResponse.Builder responseBuilder = ExampleProto.GetResponse.newBuilder();
        byte[] keyBytes = request.getKey().getBytes();
        // 从Leader节点获取Read Index，并等待Read Index之前的日志条目应用到复制状态机
        if (raftNode.waitForLeaderCommitIndex()) {
            byte[] valueBytes = stateMachine.get(keyBytes);
            if (valueBytes != null) {
                String value = new String(valueBytes);
                responseBuilder.setValue(value);
            }
        } else {
            LOG.warn("read failed, meet error");
        }
        ExampleProto.GetResponse response = responseBuilder.build();
        LOG.info("get request, request={}, response={}", jsonFormat.printToString(request),
                jsonFormat.printToString(response));
        return response;
    }

    /*@Override
    public ExampleProto.GetResponse get(ExampleProto.GetRequest request) {
        // Leader-read 强一致性（Read Index）
        ExampleProto.GetResponse.Builder responseBuilder = ExampleProto.GetResponse.newBuilder();
        if (raftNode.getLeaderId() > 0) {
            // 如果自己不是leader，将读请求转发给leader
            if (raftNode.getLeaderId() != raftNode.getLocalServer().getServerId()) {
                onLeaderChangeEvent();
                ExampleProto.GetResponse responseFromLeader = leaderService.get(request);
                responseBuilder.mergeFrom(responseFromLeader);
            } else {
                byte[] keyBytes = request.getKey().getBytes();
                // 确认当前节点仍是Leader，并等待Read Index之前的日志条目应用到复制状态机
                if (raftNode.waitUntilApplied()) {
                    // 读取复制状态机
                    byte[] valueBytes = stateMachine.get(keyBytes);
                    if (valueBytes != null) {
                        String value = new String(valueBytes);
                        responseBuilder.setValue(value);
                    }
                } else {
                    LOG.warn("read failed, meet error");
                }
            }
        }

        ExampleProto.GetResponse response = responseBuilder.build();
        LOG.info("get request, request={}, response={}", jsonFormat.printToString(request),
                jsonFormat.printToString(response));
        return response;
    }*/
}
