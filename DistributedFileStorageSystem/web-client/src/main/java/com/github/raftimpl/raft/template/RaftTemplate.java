package com.github.raftimpl.raft.template;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.github.raftimpl.raft.example.server.service.ExampleProto;
import com.github.raftimpl.raft.example.server.service.ExampleService;
import com.googlecode.protobuf.format.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class RaftTemplate {
    @Value("${raft-impl.cluster.address}")
    private String address;
    private ExampleService exampleService;
    private final JsonFormat format = new JsonFormat();

    @PostConstruct
    private void init() {
        RpcClientOptions options = new RpcClientOptions();
        options.setIoThreadNum(Runtime.getRuntime().availableProcessors() * 10);
        options.setWorkThreadNum(Runtime.getRuntime().availableProcessors() * 10);
        RpcClient rpcClient = new RpcClient("list://" + address);
        exampleService = BrpcProxy.getProxy(rpcClient, ExampleService.class);
    }

    public String read(String key) {
        ExampleProto.GetRequest request = ExampleProto.GetRequest.newBuilder()
                .setKey(key).build();
        ExampleProto.GetResponse response = exampleService.get(request);
        String result = format.printToString(response);
        log.info("读请求执行，key={}：{}", key, result);
        return result;
    }

    public String write(String key, String value) {
        ExampleProto.SetRequest request = ExampleProto.SetRequest.newBuilder()
                .setKey(key).setValue(value).build();
        ExampleProto.SetResponse response = exampleService.set(request);
        String result = format.printToString(response);
        log.info("写请求执行，key={}，value={}：{}", key, value, result);
        return result;
    }
}
