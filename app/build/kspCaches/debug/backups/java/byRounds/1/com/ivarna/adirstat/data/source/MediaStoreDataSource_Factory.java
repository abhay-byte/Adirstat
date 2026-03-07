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
public final class MediaStoreDataSource_Factory implements Factory<MediaStoreDataSource> {
  private final Provider<Context> contextProvider;

  public MediaStoreDataSource_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaStoreDataSource get() {
    return newInstance(contextProvider.get());
  }

  public static MediaStoreDataSource_Factory create(Provider<Context> contextProvider) {
    return new MediaStoreDataSource_Factory(contextProvider);
  }

  public static MediaStoreDataSource newInstance(Context context) {
    return new MediaStoreDataSource(context);
  }
}
