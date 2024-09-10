package com.github.raftimpl.raft;

/**
 * Raft状态机接口类
 * Created by raftimpl on 2017/5/10.
 */
public interface StateMachine {
    /**
     * 对状态机中数据进行snapshot，每个节点本地定时调用
     * @param snapshotDir 旧snapshot目录
     * @param tmpSnapshotDataDir 新snapshot数据目录
     * @param raftNode Raft节点
     * @param localLastAppliedIndex 已应用到复制状态机的最大日志条目索引
     */
    void writeSnapshot(String snapshotDir, String tmpSnapshotDataDir, RaftNode raftNode, long localLastAppliedIndex);

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

    /**
     * 从状态机读取数据
     * @param dataBytes Key的数据二进制
     * @return Value的数据二进制
     */
    byte[] get(byte[] dataBytes);
}
