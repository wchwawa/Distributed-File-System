import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.github.raftimpl.raft.example.server.service.ExampleProto;
import com.github.raftimpl.raft.example.server.service.ExampleService;
import com.googlecode.protobuf.format.JsonFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test {
    public static void main(String[] args) {
        String ipPorts = "list://192.168.227.117:8051,192.168.227.117:8052,192.168.227.117:8053";
        String key = "name";
        String value = "James";

        RpcClient rpcClient = new RpcClient(ipPorts);
        ExampleService exampleService = BrpcProxy.getProxy(rpcClient, ExampleService.class);
        JsonFormat format = new JsonFormat();

        ExampleProto.SetRequest request = ExampleProto.SetRequest.newBuilder()
                .setKey(key).setValue(value).build();
        ExampleProto.SetResponse response = exampleService.set(request);
        log.info("写请求执行，key={}，value={}：{}", key, value, format.printToString(response));
    }
}
