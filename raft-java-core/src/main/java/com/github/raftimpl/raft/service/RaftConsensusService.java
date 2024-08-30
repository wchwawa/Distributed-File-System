package com.github.raftimpl.raft.service;

import com.github.raftimpl.raft.proto.RaftProto;

/**
 * raft节点之间相互通信的接口。
 * Created by wchwawa on 2024/8/12.
 */
public interface RaftConsensusService {

    RaftProto.VoteResponse preVote(RaftProto.VoteRequest request);

    RaftProto.VoteResponse requestVote(RaftProto.VoteRequest request);

    RaftProto.AppendEntriesResponse appendEntries(RaftProto.AppendEntriesRequest request);

    RaftProto.InstallSnapshotResponse installSnapshot(RaftProto.InstallSnapshotRequest request);
}
