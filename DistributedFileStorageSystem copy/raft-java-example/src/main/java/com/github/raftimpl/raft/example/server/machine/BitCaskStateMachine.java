//package com.github.raftimpl.raft.example.server.machine;
//
//import com.github.raftimpl.raft.RaftNode;
//import com.github.raftimpl.raft.StateMachine;
//import com.github.raftimpl.raft.example.server.service.ExampleProto;
//import com.github.raftimpl.raft.proto.RaftProto;
//import com.github.raftimpl.raft.storage.SegmentedLog;
////import com.trifork.bitcask.BitCask;
////import com.trifork.bitcask.BitCaskOptions;
//import org.apache.commons.io.FileUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//
//public class BitCaskStateMachine implements StateMachine {
//    private static final Logger LOG = LoggerFactory.getLogger(BitCaskStateMachine.class);
////    private BitCask db;
//    private final String raftDataDir;
//
//    public BitCaskStateMachine(String raftDataDir) {
//        this.raftDataDir = raftDataDir;
//    }
//
//    @Override
//    public void writeSnapshot(String snapshotDir, String tmpSnapshotDataDir, RaftNode raftNode, long localLastAppliedIndex) {
//        try {
//            File snapshotData = new File(snapshotDir + File.separator + "data");
//            File tmpSnapshotData = new File(tmpSnapshotDataDir);
//            FileUtils.copyDirectory(snapshotData, tmpSnapshotData);
//
//            BitCaskOptions options = new BitCaskOptions();
//            options.read_write = true;
//            BitCask tmpDB = BitCask.open(tmpSnapshotData, options);
//
//            SegmentedLog raftLog = raftNode.getRaftLog();
//            for (long index = raftNode.getSnapshot().getMetaData().getLastIncludedIndex() + 1;
//                 index <= localLastAppliedIndex; index++) {
//                RaftProto.LogEntry entry = raftLog.getEntry(index);
//                if (entry.getType() == RaftProto.EntryType.ENTRY_TYPE_DATA) {
//                    ExampleProto.SetRequest request = ExampleProto.SetRequest.parseFrom(entry.getData().toByteArray());
//                    tmpDB.put(request.getKey(), request.getValue());
//                }
//            }
//
//            tmpDB.close();
//        } catch (Exception e) {
//            LOG.warn("writeSnapshot meet exception, msg={}", e.getMessage());
//        }
//    }
//
//    @Override
//    public void readSnapshot(String snapshotDir) {
//        try {
//            // 将快照目录复制到数据目录
//            if (db != null) {
//                db.close();
//                db = null;
//            }
//            String dataDir = raftDataDir + File.separator + "leveldb_data";
//            File dataFile = new File(dataDir);
//            if (dataFile.exists()) {
//                FileUtils.deleteDirectory(dataFile);
//            }
//            File snapshotFile = new File(snapshotDir);
//            if (snapshotFile.exists()) {
//                FileUtils.copyDirectory(snapshotFile, dataFile);
//            }
//
//            BitCaskOptions options = new BitCaskOptions();
//            options.read_write = true;
//            db = BitCask.open(dataFile, options);
//        } catch (Exception e) {
//            LOG.warn("meet exception, msg={}", e.getMessage());
//        }
//    }
//
//    @Override
//    public void apply(byte[] dataBytes) {
//        try {
//            ExampleProto.SetRequest request = ExampleProto.SetRequest.parseFrom(dataBytes);
//            db.put(request.getKey(), request.getValue());
//        } catch (Exception e) {
//            LOG.warn("meet exception, msg={}", e.getMessage());
//        }
//    }
//
//    @Override
//    public byte[] get(byte[] dataBytes) {
//        byte[] result = null;
//        try {
//            String value = db.getString(new String(dataBytes));
//            if (value != null) {
//                result = value.getBytes();
//            }
//        } catch (IOException e) {
//            LOG.warn("read BitCaskDB error, msg={}", e.getMessage());
//        }
//        return result;
//    }
//}
