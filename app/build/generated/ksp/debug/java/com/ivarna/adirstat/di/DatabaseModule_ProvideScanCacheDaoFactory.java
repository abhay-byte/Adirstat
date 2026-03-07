package com.ivarna.adirstat.di;

import com.ivarna.adirstat.data.local.db.AdirstatDatabase;
import com.ivarna.adirstat.data.local.db.ScanCacheDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideScanCacheDaoFactory implements Factory<ScanCacheDao> {
  private final Provider<AdirstatDatabase> databaseProvider;

  public DatabaseModule_ProvideScanCacheDaoFactory(Provider<AdirstatDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ScanCacheDao get() {
    return provideScanCacheDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideScanCacheDaoFactory create(
      Provider<AdirstatDatabase> databaseProvider) {
    return new DatabaseModule_ProvideScanCacheDaoFactory(databaseProvider);
  }

  public static ScanCacheDao provideScanCacheDao(AdirstatDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideScanCacheDao(database));
  }
}
