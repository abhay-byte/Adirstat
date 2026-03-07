package com.ivarna.adirstat.data.repository;

import com.google.gson.Gson;
import com.ivarna.adirstat.data.local.db.ScanCacheDao;
import com.ivarna.adirstat.data.source.AppStatsDataSource;
import com.ivarna.adirstat.data.source.FileSystemDataSource;
import com.ivarna.adirstat.data.source.StorageVolumeDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class StorageRepositoryImpl_Factory implements Factory<StorageRepositoryImpl> {
  private final Provider<FileSystemDataSource> fileSystemDataSourceProvider;

  private final Provider<StorageVolumeDataSource> storageVolumeDataSourceProvider;

  private final Provider<AppStatsDataSource> appStatsDataSourceProvider;

  private final Provider<ScanCacheDao> scanCacheDaoProvider;

  private final Provider<Gson> gsonProvider;

  public StorageRepositoryImpl_Factory(Provider<FileSystemDataSource> fileSystemDataSourceProvider,
      Provider<StorageVolumeDataSource> storageVolumeDataSourceProvider,
      Provider<AppStatsDataSource> appStatsDataSourceProvider,
      Provider<ScanCacheDao> scanCacheDaoProvider, Provider<Gson> gsonProvider) {
    this.fileSystemDataSourceProvider = fileSystemDataSourceProvider;
    this.storageVolumeDataSourceProvider = storageVolumeDataSourceProvider;
    this.appStatsDataSourceProvider = appStatsDataSourceProvider;
    this.scanCacheDaoProvider = scanCacheDaoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public StorageRepositoryImpl get() {
    return newInstance(fileSystemDataSourceProvider.get(), storageVolumeDataSourceProvider.get(), appStatsDataSourceProvider.get(), scanCacheDaoProvider.get(), gsonProvider.get());
  }

  public static StorageRepositoryImpl_Factory create(
      Provider<FileSystemDataSource> fileSystemDataSourceProvider,
      Provider<StorageVolumeDataSource> storageVolumeDataSourceProvider,
      Provider<AppStatsDataSource> appStatsDataSourceProvider,
      Provider<ScanCacheDao> scanCacheDaoProvider, Provider<Gson> gsonProvider) {
    return new StorageRepositoryImpl_Factory(fileSystemDataSourceProvider, storageVolumeDataSourceProvider, appStatsDataSourceProvider, scanCacheDaoProvider, gsonProvider);
  }

  public static StorageRepositoryImpl newInstance(FileSystemDataSource fileSystemDataSource,
      StorageVolumeDataSource storageVolumeDataSource, AppStatsDataSource appStatsDataSource,
      ScanCacheDao scanCacheDao, Gson gson) {
    return new StorageRepositoryImpl(fileSystemDataSource, storageVolumeDataSource, appStatsDataSource, scanCacheDao, gson);
  }
}
