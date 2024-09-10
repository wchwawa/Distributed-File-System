package com.github.raftimpl.raft.example.server.machine;

import btree4j.BTreeException;
import btree4j.BTreeIndex;
import btree4j.Value;
import com.github.raftimpl.raft.RaftNode;
import com.github.raftimpl.raft.StateMachine;
import com.github.raftimpl.raft.example.server.service.ExampleProto;
import com.github.raftimpl.raft.proto.RaftProto;
import com.github.raftimpl.raft.storage.SegmentedLog;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class BTreeStateMachine implements StateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(BTreeStateMachine.class);
    private BTreeIndex db;
    private final String raftDataDir;
    private final String FILENAME = "btree.data";
    private final FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.equals(FILENAME);
        }
    };

    public BTreeStateMachine(String raftDataDir) {
        this.raftDataDir = raftDataDir;
    }

    @Override
    public void writeSnapshot(String snapshotDir, String tmpSnapshotDataDir, RaftNode raftNode, long localLastAppliedIndex) {
        try {
            File snapshotData = new File(snapshotDir + File.separator + "data");
            File tmpSnapshotData = new File(tmpSnapshotDataDir);
            FileUtils.copyDirectory(snapshotData, tmpSnapshotData);

            String[] files = tmpSnapshotData.list(filenameFilter);
            if (files == null) {
                throw new IOException("error when handling data directory, please check " + raftDataDir);
            }
            File currentFile = files.length == 0 ?
                    new File(tmpSnapshotData, FILENAME)
                    : new File(files[0]);

            BTreeIndex tmpDB = new BTreeIndex(currentFile);
            tmpDB.init(false);

            SegmentedLog raftLog = raftNode.getRaftLog();
            for (long index = raftNode.getSnapshot().getMetaData().getLastIncludedIndex() + 1;
                 index <= localLastAppliedIndex; index++) {
                RaftProto.LogEntry entry = raftLog.getEntry(index);
                if (entry.getType() == RaftProto.EntryType.ENTRY_TYPE_DATA) {
                    ExampleProto.SetRequest request = ExampleProto.SetRequest.parseFrom(entry.getData().toByteArray());
                    tmpDB.putValue(new Value(request.getKey()), new Value(request.getValue()));
                }
            }

            tmpDB.flush();
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
            String dataDir = raftDataDir + File.separator + "btree_data";
            File dataFile = new File(dataDir);
            if (dataFile.exists()) {
                FileUtils.deleteDirectory(dataFile);
            }
            File snapshotFile = new File(snapshotDir);
            if (snapshotFile.exists()) {
                FileUtils.copyDirectory(snapshotFile, dataFile);
            } else if (!dataFile.mkdirs()) {
                throw new IOException("error when handling data directory, please check " + raftDataDir);
            }

            // 将快照目录复制到数据目录后，检查数据目录下是否存在数据文件，如果有则打开数据文件，如果没有则创建新的数据文件
            String[] files = dataFile.list(filenameFilter);
            if (files == null) {
                throw new IOException("error when handling data directory, please check " + raftDataDir);
            }
            File currentFile = files.length == 0 ?
                    new File(dataFile, FILENAME)
                    : new File(files[0]);

            db = new BTreeIndex(currentFile);
            db.init(false);
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
            db.putValue(new Value(request.getKey()), new Value(request.getValue()));
            db.flush();
        } catch (Exception e) {
            LOG.warn("meet exception, msg={}", e.getMessage());
        }
    }

    @Override
    public byte[] get(byte[] dataBytes) {
        byte[] result = null;
        try {
            if (db == null) {
                throw new BTreeException("database is closed, please wait for reopen");
            }
            Value value = db.getValue(new Value(new String(dataBytes)));
            if (value != null) {
                result = value.toString().getBytes();
            }
        } catch (Exception e) {
            LOG.warn("read BTreeDB exception, msg={}", e.getMessage());
        }
        return result;
    }
}
