package com.github.raftimpl.raft.example.server.machine;

import btree4j.BTreeException;
import com.github.raftimpl.raft.RaftNode;
import com.github.raftimpl.raft.StateMachine;
import com.github.raftimpl.raft.example.server.service.ExampleProto;
import com.github.raftimpl.raft.proto.RaftProto;
import com.github.raftimpl.raft.storage.SegmentedLog;
import org.apache.commons.io.FileUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LevelDBStateMachine implements StateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(LevelDBStateMachine.class);
    private DB db;
    private final String raftDataDir;

    public LevelDBStateMachine(String raftDataDir) {
        this.raftDataDir = raftDataDir;
    }

    @Override
    public void writeSnapshot(String snapshotDir, String tmpSnapshotDataDir, RaftNode raftNode, long localLastAppliedIndex) {
        try {
            File snapshotData = new File(snapshotDir + File.separator + "data");
            File tmpSnapshotData = new File(tmpSnapshotDataDir);
            FileUtils.copyDirectory(snapshotData, tmpSnapshotData);

            Options options = new Options();
            DB tmpDB = Iq80DBFactory.factory.open(tmpSnapshotData, options);

            SegmentedLog raftLog = raftNode.getRaftLog();
            for (long index = raftNode.getSnapshot().getMetaData().getLastIncludedIndex() + 1;
                 index <= localLastAppliedIndex; index++) {
                RaftProto.LogEntry entry = raftLog.getEntry(index);
                if (entry.getType() == RaftProto.EntryType.ENTRY_TYPE_DATA) {
                    ExampleProto.SetRequest request = ExampleProto.SetRequest.parseFrom(entry.getData().toByteArray());
                    tmpDB.put(request.getKey().getBytes(), request.getValue().getBytes());
                }
            }

            tmpDB.close();
        } catch (Exception e) {
            LOG.warn("writeSnapshot meet exception, msg={}", e.getMessage());
        }
    }

    @Override
    public void readSnapshot(String snapshotDir) {
        try {
            // 将快照目录复制到数据目录
            if (db != null) {
                db.close();
                db = null;
            }
            String dataDir = raftDataDir + File.separator + "leveldb_data";
            File dataFile = new File(dataDir);
            if (dataFile.exists()) {
                FileUtils.deleteDirectory(dataFile);
            }
            File snapshotFile = new File(snapshotDir);
            if (snapshotFile.exists()) {
                FileUtils.copyDirectory(snapshotFile, dataFile);
            }

            Options options = new Options();
            db = Iq80DBFactory.factory.open(dataFile, options);
        } catch (Exception e) {
            LOG.warn("meet exception, msg={}", e.getMessage());
        }
    }

    @Override
    public void apply(byte[] dataBytes) {
        try {
            if (db == null) {
                throw new BTreeException("database is closed, please wait for reopen");
            }
            ExampleProto.SetRequest request = ExampleProto.SetRequest.parseFrom(dataBytes);
            db.put(request.getKey().getBytes(), request.getValue().getBytes());
        } catch (Exception e) {
            LOG.warn("meet exception, msg={}", e.getMessage());
        }
    }

    @Override
    public byte[] get(byte[] dataBytes) {
        byte[] result = null;
        try {
            if (db == null) {
                throw new DBException("database is closed, please wait for reopen");
            }
            result = db.get(dataBytes);
        } catch (Exception e) {
            LOG.warn("read leveldb exception, msg={}", e.getMessage());
        }
        return result;
    }
}
