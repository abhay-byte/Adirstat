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
public final class ScanCacheDao_Impl implements ScanCacheDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScanCacheEntity> __insertionAdapterOfScanCacheEntity;

  private final EntityDeletionOrUpdateAdapter<ScanCacheEntity> __deletionAdapterOfScanCacheEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCacheById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCacheForScan;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllCache;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldCache;

  public ScanCacheDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScanCacheEntity = new EntityInsertionAdapter<ScanCacheEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `scan_cache` (`id`,`scanHistoryId`,`serializedTreeJson`,`createdAt`,`partitionPath`,`totalSize`,`fileCount`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanCacheEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getScanHistoryId());
        statement.bindString(3, entity.getSerializedTreeJson());
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindString(5, entity.getPartitionPath());
        statement.bindLong(6, entity.getTotalSize());
        statement.bindLong(7, entity.getFileCount());
      }
    };
    this.__deletionAdapterOfScanCacheEntity = new EntityDeletionOrUpdateAdapter<ScanCacheEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `scan_cache` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanCacheEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCacheById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_cache WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteCacheForScan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_cache WHERE scanHistoryId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllCache = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_cache";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldCache = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_cache WHERE createdAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCache(final ScanCacheEntity cache,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfScanCacheEntity.insertAndReturnId(cache);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCache(final ScanCacheEntity cache,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfScanCacheEntity.handle(cache);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCacheById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCacheById.acquire();
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
          __preparedStmtOfDeleteCacheById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCacheForScan(final long scanHistoryId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCacheForScan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, scanHistoryId);
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
          __preparedStmtOfDeleteCacheForScan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllCache(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllCache.acquire();
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
          __preparedStmtOfDeleteAllCache.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldCache(final long timestamp, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldCache.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
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
          __preparedStmtOfDeleteOldCache.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getCacheForScan(final long scanHistoryId,
      final Continuation<? super ScanCacheEntity> $completion) {
    final String _sql = "SELECT * FROM scan_cache WHERE scanHistoryId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, scanHistoryId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScanCacheEntity>() {
      @Override
      @Nullable
      public ScanCacheEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfScanHistoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "scanHistoryId");
          final int _cursorIndexOfSerializedTreeJson = CursorUtil.getColumnIndexOrThrow(_cursor, "serializedTreeJson");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfPartitionPath = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionPath");
          final int _cursorIndexOfTotalSize = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSize");
          final int _cursorIndexOfFileCount = CursorUtil.getColumnIndexOrThrow(_cursor, "fileCount");
          final ScanCacheEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpScanHistoryId;
            _tmpScanHistoryId = _cursor.getLong(_cursorIndexOfScanHistoryId);
            final String _tmpSerializedTreeJson;
            _tmpSerializedTreeJson = _cursor.getString(_cursorIndexOfSerializedTreeJson);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpPartitionPath;
            _tmpPartitionPath = _cursor.getString(_cursorIndexOfPartitionPath);
            final long _tmpTotalSize;
            _tmpTotalSize = _cursor.getLong(_cursorIndexOfTotalSize);
            final long _tmpFileCount;
            _tmpFileCount = _cursor.getLong(_cursorIndexOfFileCount);
            _result = new ScanCacheEntity(_tmpId,_tmpScanHistoryId,_tmpSerializedTreeJson,_tmpCreatedAt,_tmpPartitionPath,_tmpTotalSize,_tmpFileCount);
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
  public Object getLatestCacheForPartition(final String path,
      final Continuation<? super ScanCacheEntity> $completion) {
    final String _sql = "SELECT * FROM scan_cache WHERE partitionPath = ? ORDER BY createdAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, path);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScanCacheEntity>() {
      @Override
      @Nullable
      public ScanCacheEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfScanHistoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "scanHistoryId");
          final int _cursorIndexOfSerializedTreeJson = CursorUtil.getColumnIndexOrThrow(_cursor, "serializedTreeJson");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfPartitionPath = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionPath");
          final int _cursorIndexOfTotalSize = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSize");
          final int _cursorIndexOfFileCount = CursorUtil.getColumnIndexOrThrow(_cursor, "fileCount");
          final ScanCacheEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpScanHistoryId;
            _tmpScanHistoryId = _cursor.getLong(_cursorIndexOfScanHistoryId);
            final String _tmpSerializedTreeJson;
            _tmpSerializedTreeJson = _cursor.getString(_cursorIndexOfSerializedTreeJson);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpPartitionPath;
            _tmpPartitionPath = _cursor.getString(_cursorIndexOfPartitionPath);
            final long _tmpTotalSize;
            _tmpTotalSize = _cursor.getLong(_cursorIndexOfTotalSize);
            final long _tmpFileCount;
            _tmpFileCount = _cursor.getLong(_cursorIndexOfFileCount);
            _result = new ScanCacheEntity(_tmpId,_tmpScanHistoryId,_tmpSerializedTreeJson,_tmpCreatedAt,_tmpPartitionPath,_tmpTotalSize,_tmpFileCount);
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
  public Flow<List<ScanCacheEntity>> getAllCache() {
    final String _sql = "SELECT * FROM scan_cache ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_cache"}, new Callable<List<ScanCacheEntity>>() {
      @Override
      @NonNull
      public List<ScanCacheEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfScanHistoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "scanHistoryId");
          final int _cursorIndexOfSerializedTreeJson = CursorUtil.getColumnIndexOrThrow(_cursor, "serializedTreeJson");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfPartitionPath = CursorUtil.getColumnIndexOrThrow(_cursor, "partitionPath");
          final int _cursorIndexOfTotalSize = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSize");
          final int _cursorIndexOfFileCount = CursorUtil.getColumnIndexOrThrow(_cursor, "fileCount");
          final List<ScanCacheEntity> _result = new ArrayList<ScanCacheEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanCacheEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpScanHistoryId;
            _tmpScanHistoryId = _cursor.getLong(_cursorIndexOfScanHistoryId);
            final String _tmpSerializedTreeJson;
            _tmpSerializedTreeJson = _cursor.getString(_cursorIndexOfSerializedTreeJson);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpPartitionPath;
            _tmpPartitionPath = _cursor.getString(_cursorIndexOfPartitionPath);
            final long _tmpTotalSize;
            _tmpTotalSize = _cursor.getLong(_cursorIndexOfTotalSize);
            final long _tmpFileCount;
            _tmpFileCount = _cursor.getLong(_cursorIndexOfFileCount);
            _item = new ScanCacheEntity(_tmpId,_tmpScanHistoryId,_tmpSerializedTreeJson,_tmpCreatedAt,_tmpPartitionPath,_tmpTotalSize,_tmpFileCount);
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
  public Object getCacheCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM scan_cache";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
