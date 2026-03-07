package com.ivarna.adirstat.data.source;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppStatsDataSource_Factory implements Factory<AppStatsDataSource> {
  private final Provider<Context> contextProvider;

  public AppStatsDataSource_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppStatsDataSource get() {
    return newInstance(contextProvider.get());
  }

  public static AppStatsDataSource_Factory create(Provider<Context> contextProvider) {
    return new AppStatsDataSource_Factory(contextProvider);
  }

  public static AppStatsDataSource newInstance(Context context) {
    return new AppStatsDataSource(context);
  }
}
