package com.ivarna.adirstat.data.local.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AdirstatDatabase_Impl extends AdirstatDatabase {
  private volatile ScanHistoryDao _scanHistoryDao;

  private volatile ScanCacheDao _scanCacheDao;

  private volatile UserExclusionDao _userExclusionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `scan_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `partitionPath` TEXT NOT NULL, `partitionName` TEXT NOT NULL, `scanDate` INTEGER NOT NULL, `totalBytes` INTEGER NOT NULL, `freeBytes` INTEGER NOT NULL, `usedBytes` INTEGER NOT NULL, `fileCount` INTEGER NOT NULL, `folderCount` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `scanType` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `scan_cache` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scanHistoryId` INTEGER NOT NULL, `serializedTreeJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `partitionPath` TEXT NOT NULL, `totalSize` INTEGER NOT NULL, `fileCount` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_exclusions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `path` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `isActive` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c2a14ce08173ee5a6dd05526bb134f0e')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `scan_history`");
        db.execSQL("DROP TABLE IF EXISTS `scan_cache`");
        db.execSQL("DROP TABLE IF EXISTS `user_exclusions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsScanHistory = new HashMap<String, TableInfo.Column>(11);
        _columnsScanHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("partitionPath", new TableInfo.Column("partitionPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("partitionName", new TableInfo.Column("partitionName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("scanDate", new TableInfo.Column("scanDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("totalBytes", new TableInfo.Column("totalBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("freeBytes", new TableInfo.Column("freeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("usedBytes", new TableInfo.Column("usedBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("fileCount", new TableInfo.Column("fileCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("folderCount", new TableInfo.Column("folderCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanHistory.put("scanType", new TableInfo.Column("scanType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScanHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScanHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoScanHistory = new TableInfo("scan_history", _columnsScanHistory, _foreignKeysScanHistory, _indicesScanHistory);
        final TableInfo _existingScanHistory = TableInfo.read(db, "scan_history");
        if (!_infoScanHistory.equals(_existingScanHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "scan_history(com.ivarna.adirstat.data.local.db.ScanHistoryEntity).\n"
                  + " Expected:\n" + _infoScanHistory + "\n"
                  + " Found:\n" + _existingScanHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsScanCache = new HashMap<String, TableInfo.Column>(7);
        _columnsScanCache.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanCache.put("scanHistoryId", new TableInfo.Column("scanHistoryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanCache.put("serializedTreeJson", new TableInfo.Column("serializedTreeJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanCache.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanCache.put("partitionPath", new TableInfo.Column("partitionPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanCache.put("totalSize", new TableInfo.Column("totalSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScanCache.put("fileCount", new TableInfo.Column("fileCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScanCache = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScanCache = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoScanCache = new TableInfo("scan_cache", _columnsScanCache, _foreignKeysScanCache, _indicesScanCache);
        final TableInfo _existingScanCache = TableInfo.read(db, "scan_cache");
        if (!_infoScanCache.equals(_existingScanCache)) {
          return new RoomOpenHelper.ValidationResult(false, "scan_cache(com.ivarna.adirstat.data.local.db.ScanCacheEntity).\n"
                  + " Expected:\n" + _infoScanCache + "\n"
                  + " Found:\n" + _existingScanCache);
        }
        final HashMap<String, TableInfo.Column> _columnsUserExclusions = new HashMap<String, TableInfo.Column>(4);
        _columnsUserExclusions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserExclusions.put("path", new TableInfo.Column("path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserExclusions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserExclusions.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserExclusions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserExclusions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserExclusions = new TableInfo("user_exclusions", _columnsUserExclusions, _foreignKeysUserExclusions, _indicesUserExclusions);
        final TableInfo _existingUserExclusions = TableInfo.read(db, "user_exclusions");
        if (!_infoUserExclusions.equals(_existingUserExclusions)) {
          return new RoomOpenHelper.ValidationResult(false, "user_exclusions(com.ivarna.adirstat.data.local.db.UserExclusionEntity).\n"
                  + " Expected:\n" + _infoUserExclusions + "\n"
                  + " Found:\n" + _existingUserExclusions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c2a14ce08173ee5a6dd05526bb134f0e", "b6e3144a425f60385980f5f14896ccdd");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "scan_history","scan_cache","user_exclusions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `scan_history`");
      _db.execSQL("DELETE FROM `scan_cache`");
      _db.execSQL("DELETE FROM `user_exclusions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ScanHistoryDao.class, ScanHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ScanCacheDao.class, ScanCacheDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserExclusionDao.class, UserExclusionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ScanHistoryDao scanHistoryDao() {
    if (_scanHistoryDao != null) {
      return _scanHistoryDao;
    } else {
      synchronized(this) {
        if(_scanHistoryDao == null) {
          _scanHistoryDao = new ScanHistoryDao_Impl(this);
        }
        return _scanHistoryDao;
      }
    }
  }

  @Override
  public ScanCacheDao scanCacheDao() {
    if (_scanCacheDao != null) {
      return _scanCacheDao;
    } else {
      synchronized(this) {
        if(_scanCacheDao == null) {
          _scanCacheDao = new ScanCacheDao_Impl(this);
        }
        return _scanCacheDao;
      }
    }
  }

  @Override
  public UserExclusionDao userExclusionDao() {
    if (_userExclusionDao != null) {
      return _userExclusionDao;
    } else {
      synchronized(this) {
        if(_userExclusionDao == null) {
          _userExclusionDao = new UserExclusionDao_Impl(this);
        }
        return _userExclusionDao;
      }
    }
  }
}
