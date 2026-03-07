package com.ivarna.adirstat.di;

import com.ivarna.adirstat.data.local.db.AdirstatDatabase;
import com.ivarna.adirstat.data.local.db.UserExclusionDao;
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
public final class DatabaseModule_ProvideUserExclusionDaoFactory implements Factory<UserExclusionDao> {
  private final Provider<AdirstatDatabase> databaseProvider;

  public DatabaseModule_ProvideUserExclusionDaoFactory(
      Provider<AdirstatDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public UserExclusionDao get() {
    return provideUserExclusionDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideUserExclusionDaoFactory create(
      Provider<AdirstatDatabase> databaseProvider) {
    return new DatabaseModule_ProvideUserExclusionDaoFactory(databaseProvider);
  }

  public static UserExclusionDao provideUserExclusionDao(AdirstatDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideUserExclusionDao(database));
  }
}
