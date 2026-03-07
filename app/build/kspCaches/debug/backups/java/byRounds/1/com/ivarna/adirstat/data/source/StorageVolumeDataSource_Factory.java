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
public final class StorageVolumeDataSource_Factory implements Factory<StorageVolumeDataSource> {
  private final Provider<Context> contextProvider;

  public StorageVolumeDataSource_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public StorageVolumeDataSource get() {
    return newInstance(contextProvider.get());
  }

  public static StorageVolumeDataSource_Factory create(Provider<Context> contextProvider) {
    return new StorageVolumeDataSource_Factory(contextProvider);
  }

  public static StorageVolumeDataSource newInstance(Context context) {
    return new StorageVolumeDataSource(context);
  }
}
