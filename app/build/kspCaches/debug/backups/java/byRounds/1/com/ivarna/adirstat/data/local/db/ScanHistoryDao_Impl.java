package com.ivarna.adirstat.data.local.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScanHistoryDao_Impl implements ScanHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScanHistoryEntity> __insertionAdapterOfScanHistoryEntity;

  private final EntityDeletionOrUpdateAdapter<ScanHistoryEntity> __deletionAdapterOfScanHistoryEntity;

  private final EntityDeletionOrUpdateAdapter<ScanHistoryEntity> __updateAdapterOfScanHistoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteScanById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllScans;

  public ScanHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScanHistoryEntity = new EntityInsertionAdapter<ScanHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `scan_history` (`id`,`partitionPath`,`partitionName`,`scanDate`,`totalBytes`,`freeBytes`,`usedBytes`,`fileCount`,`folderCount`,`durationMs`,`scanType`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPartitionPath());
        statement.bindString(3, entity.getPartitionName());
        statement.bindLong(4, entity.getScanDate());
        statement.bindLong(5, entity.getTotalBytes());
        statement.bindLong(6, entity.getFreeBytes());
        statement.bindLong(7, entity.getUsedBytes());
        statement.bindLong(8, entity.getFileCount());
        statement.bindLong(9, entity.getFolderCount());
        statement.bindLong(10, entity.getDurationMs());
        statement.bindString(11, entity.getScanType());
      }
    };
    this.__deletionAdapterOfScanHistoryEntity = new EntityDeletionOrUpdateAdapter<ScanHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `scan_history` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfScanHistoryEntity = new EntityDeletionOrUpdateAdapter<ScanHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `scan_history` SET `id` = ?,`partitionPath` = ?,`partitionName` = ?,`scanDate` = ?,`totalBytes` = ?,`freeBytes` = ?,`usedBytes` = ?,`fileCount` = ?,`folderCount` = ?,`durationMs` = ?,`scanType` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPartitionPath());
        statement.bindString(3, entity.getPartitionName());
        statement.bindLong(4, entity.getScanDate());
        statement.bindLong(5, entity.getTotalBytes());
        statement.bindLong(6, entity.getFreeBytes());
        statement.bindLong(7, entity.getUsedBytes());
        statement.bindLong(8, entity.getFileCount());
        statement.bindLong(9, entity.getFolderCount());
        statement.bindLong(10, entity.getDurationMs());
        statement.bindString(11, entity.getScanType());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteScanById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_history WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllScans = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_history";
        return _query;
      }
    };
  }

  @Override
  public Object insertScan(final ScanHistoryEntity scan,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfScanHistoryEntity.insertAndReturnId(scan);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteScan(final ScanHistoryEntity scan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfScanHistoryEntity.handle(scan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateScan(final ScanHistoryEntity scan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfScanHistoryEntity.handle(scan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteScanById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteScanById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteScanById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllScans(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllScans.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllScans.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScanHistoryEntity>> getAllScans() {
    final String _sql = "SELECT * FROM scan_history ORDER BY scanDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_history"}, new Callable<List<ScanHistoryEntity>>() {
      @Override
      @NonNull
      public List<ScanHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPartitionPath = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionPath");
          final int _cursorIndexOfPartitionName = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionName");
          final int _cursorIndexOfScanDate = CursorUtil.getColumnIndexOrThrow(_cursor, "scanDate");
          final int _cursorIndexOfTotalBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBytes");
          final int _cursorIndexOfFreeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "freeBytes");
          final int _cursorIndexOfUsedBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "usedBytes");
          final int _cursorIndexOfFileCount = CursorUtil.getColumnIndexOrThrow(_cursor, "fileCount");
          final int _cursorIndexOfFolderCount = CursorUtil.getColumnIndexOrThrow(_cursor, "folderCount");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfScanType = CursorUtil.getColumnIndexOrThrow(_cursor, "scanType");
          final List<ScanHistoryEntity> _result = new ArrayList<ScanHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanHistoryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPartitionPath;
            _tmpPartitionPath = _cursor.getString(_cursorIndexOfPartitionPath);
            final String _tmpPartitionName;
            _tmpPartitionName = _cursor.getString(_cursorIndexOfPartitionName);
            final long _tmpScanDate;
            _tmpScanDate = _cursor.getLong(_cursorIndexOfScanDate);
            final long _tmpTotalBytes;
            _tmpTotalBytes = _cursor.getLong(_cursorIndexOfTotalBytes);
            final long _tmpFreeBytes;
            _tmpFreeBytes = _cursor.getLong(_cursorIndexOfFreeBytes);
            final long _tmpUsedBytes;
            _tmpUsedBytes = _cursor.getLong(_cursorIndexOfUsedBytes);
            final long _tmpFileCount;
            _tmpFileCount = _cursor.getLong(_cursorIndexOfFileCount);
            final long _tmpFolderCount;
            _tmpFolderCount = _cursor.getLong(_cursorIndexOfFolderCount);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpScanType;
            _tmpScanType = _cursor.getString(_cursorIndexOfScanType);
            _item = new ScanHistoryEntity(_tmpId,_tmpPartitionPath,_tmpPartitionName,_tmpScanDate,_tmpTotalBytes,_tmpFreeBytes,_tmpUsedBytes,_tmpFileCount,_tmpFolderCount,_tmpDurationMs,_tmpScanType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getScanById(final long id,
      final Continuation<? super ScanHistoryEntity> $completion) {
    final String _sql = "SELECT * FROM scan_history WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScanHistoryEntity>() {
      @Override
      @Nullable
      public ScanHistoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPartitionPath = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionPath");
          final int _cursorIndexOfPartitionName = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionName");
          final int _cursorIndexOfScanDate = CursorUtil.getColumnIndexOrThrow(_cursor, "scanDate");
          final int _cursorIndexOfTotalBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBytes");
          final int _cursorIndexOfFreeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "freeBytes");
          final int _cursorIndexOfUsedBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "usedBytes");
          final int _cursorIndexOfFileCount = CursorUtil.getColumnIndexOrThrow(_cursor, "fileCount");
          final int _cursorIndexOfFolderCount = CursorUtil.getColumnIndexOrThrow(_cursor, "folderCount");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfScanType = CursorUtil.getColumnIndexOrThrow(_cursor, "scanType");
          final ScanHistoryEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPartitionPath;
            _tmpPartitionPath = _cursor.getString(_cursorIndexOfPartitionPath);
            final String _tmpPartitionName;
            _tmpPartitionName = _cursor.getString(_cursorIndexOfPartitionName);
            final long _tmpScanDate;
            _tmpScanDate = _cursor.getLong(_cursorIndexOfScanDate);
            final long _tmpTotalBytes;
            _tmpTotalBytes = _cursor.getLong(_cursorIndexOfTotalBytes);
            final long _tmpFreeBytes;
            _tmpFreeBytes = _cursor.getLong(_cursorIndexOfFreeBytes);
            final long _tmpUsedBytes;
            _tmpUsedBytes = _cursor.getLong(_cursorIndexOfUsedBytes);
            final long _tmpFileCount;
            _tmpFileCount = _cursor.getLong(_cursorIndexOfFileCount);
            final long _tmpFolderCount;
            _tmpFolderCount = _cursor.getLong(_cursorIndexOfFolderCount);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpScanType;
            _tmpScanType = _cursor.getString(_cursorIndexOfScanType);
            _result = new ScanHistoryEntity(_tmpId,_tmpPartitionPath,_tmpPartitionName,_tmpScanDate,_tmpTotalBytes,_tmpFreeBytes,_tmpUsedBytes,_tmpFileCount,_tmpFolderCount,_tmpDurationMs,_tmpScanType);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestScanForPartition(final String path,
      final Continuation<? super ScanHistoryEntity> $completion) {
    final String _sql = "SELECT * FROM scan_history WHERE partitionPath = ? ORDER BY scanDate DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, path);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScanHistoryEntity>() {
      @Override
      @Nullable
      public ScanHistoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPartitionPath = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionPath");
          final int _cursorIndexOfPartitionName = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionName");
          final int _cursorIndexOfScanDate = CursorUtil.getColumnIndexOrThrow(_cursor, "scanDate");
          final int _cursorIndexOfTotalBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBytes");
          final int _cursorIndexOfFreeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "freeBytes");
          final int _cursorIndexOfUsedBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "usedBytes");
          final int _cursorIndexOfFileCount = CursorUtil.getColumnIndexOrThrow(_cursor, "fileCount");
          final int _cursorIndexOfFolderCount = CursorUtil.getColumnIndexOrThrow(_cursor, "folderCount");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfScanType = CursorUtil.getColumnIndexOrThrow(_cursor, "scanType");
          final ScanHistoryEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPartitionPath;
            _tmpPartitionPath = _cursor.getString(_cursorIndexOfPartitionPath);
            final String _tmpPartitionName;
            _tmpPartitionName = _cursor.getString(_cursorIndexOfPartitionName);
            final long _tmpScanDate;
            _tmpScanDate = _cursor.getLong(_cursorIndexOfScanDate);
            final long _tmpTotalBytes;
            _tmpTotalBytes = _cursor.getLong(_cursorIndexOfTotalBytes);
            final long _tmpFreeBytes;
            _tmpFreeBytes = _cursor.getLong(_cursorIndexOfFreeBytes);
            final long _tmpUsedBytes;
            _tmpUsedBytes = _cursor.getLong(_cursorIndexOfUsedBytes);
            final long _tmpFileCount;
            _tmpFileCount = _cursor.getLong(_cursorIndexOfFileCount);
            final long _tmpFolderCount;
            _tmpFolderCount = _cursor.getLong(_cursorIndexOfFolderCount);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpScanType;
            _tmpScanType = _cursor.getString(_cursorIndexOfScanType);
            _result = new ScanHistoryEntity(_tmpId,_tmpPartitionPath,_tmpPartitionName,_tmpScanDate,_tmpTotalBytes,_tmpFreeBytes,_tmpUsedBytes,_tmpFileCount,_tmpFolderCount,_tmpDurationMs,_tmpScanType);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScanHistoryEntity>> getRecentScans(final int limit) {
    final String _sql = "SELECT * FROM scan_history ORDER BY scanDate DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_history"}, new Callable<List<ScanHistoryEntity>>() {
      @Override
      @NonNull
      public List<ScanHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPartitionPath = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionPath");
          final int _cursorIndexOfPartitionName = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionName");
          final int _cursorIndexOfScanDate = CursorUtil.getColumnIndexOrThrow(_cursor, "scanDate");
          final int _cursorIndexOfTotalBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalBytes");
          final int _cursorIndexOfFreeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "freeBytes");
          final int _cursorIndexOfUsedBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "usedBytes");
          final int _cursorIndexOfFileCount = CursorUtil.getColumnIndexOrThrow(_cursor, "fileCount");
          final int _cursorIndexOfFolderCount = CursorUtil.getColumnIndexOrThrow(_cursor, "folderCount");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfScanType = CursorUtil.getColumnIndexOrThrow(_cursor, "scanType");
          final List<ScanHistoryEntity> _result = new ArrayList<ScanHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanHistoryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPartitionPath;
            _tmpPartitionPath = _cursor.getString(_cursorIndexOfPartitionPath);
            final String _tmpPartitionName;
            _tmpPartitionName = _cursor.getString(_cursorIndexOfPartitionName);
            final long _tmpScanDate;
            _tmpScanDate = _cursor.getLong(_cursorIndexOfScanDate);
            final long _tmpTotalBytes;
            _tmpTotalBytes = _cursor.getLong(_cursorIndexOfTotalBytes);
            final long _tmpFreeBytes;
            _tmpFreeBytes = _cursor.getLong(_cursorIndexOfFreeBytes);
            final long _tmpUsedBytes;
            _tmpUsedBytes = _cursor.getLong(_cursorIndexOfUsedBytes);
            final long _tmpFileCount;
            _tmpFileCount = _cursor.getLong(_cursorIndexOfFileCount);
            final long _tmpFolderCount;
            _tmpFolderCount = _cursor.getLong(_cursorIndexOfFolderCount);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpScanType;
            _tmpScanType = _cursor.getString(_cursorIndexOfScanType);
            _item = new ScanHistoryEntity(_tmpId,_tmpPartitionPath,_tmpPartitionName,_tmpScanDate,_tmpTotalBytes,_tmpFreeBytes,_tmpUsedBytes,_tmpFileCount,_tmpFolderCount,_tmpDurationMs,_tmpScanType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getScanCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM scan_history";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalScannedSize(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT SUM(totalBytes) FROM scan_history";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
