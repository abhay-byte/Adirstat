package com.ivarna.adirstat.di;

import com.ivarna.adirstat.data.local.db.AdirstatDatabase;
import com.ivarna.adirstat.data.local.db.ScanHistoryDao;
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
public final class DatabaseModule_ProvideScanHistoryDaoFactory implements Factory<ScanHistoryDao> {
  private final Provider<AdirstatDatabase> databaseProvider;

  public DatabaseModule_ProvideScanHistoryDaoFactory(Provider<AdirstatDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ScanHistoryDao get() {
    return provideScanHistoryDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideScanHistoryDaoFactory create(
      Provider<AdirstatDatabase> databaseProvider) {
    return new DatabaseModule_ProvideScanHistoryDaoFactory(databaseProvider);
  }

  public static ScanHistoryDao provideScanHistoryDao(AdirstatDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideScanHistoryDao(database));
  }
}
