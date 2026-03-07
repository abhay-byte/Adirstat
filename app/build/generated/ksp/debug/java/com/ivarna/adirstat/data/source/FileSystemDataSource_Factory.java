package com.ivarna.adirstat.data.source;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class FileSystemDataSource_Factory implements Factory<FileSystemDataSource> {
  @Override
  public FileSystemDataSource get() {
    return newInstance();
  }

  public static FileSystemDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FileSystemDataSource newInstance() {
    return new FileSystemDataSource();
  }

  private static final class InstanceHolder {
    private static final FileSystemDataSource_Factory INSTANCE = new FileSystemDataSource_Factory();
  }
}
