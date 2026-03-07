package com.ivarna.adirstat.domain.usecase;

import com.ivarna.adirstat.domain.repository.StorageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ScanStorageUseCase_Factory implements Factory<ScanStorageUseCase> {
  private final Provider<StorageRepository> repositoryProvider;

  public ScanStorageUseCase_Factory(Provider<StorageRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScanStorageUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScanStorageUseCase_Factory create(Provider<StorageRepository> repositoryProvider) {
    return new ScanStorageUseCase_Factory(repositoryProvider);
  }

  public static ScanStorageUseCase newInstance(StorageRepository repository) {
    return new ScanStorageUseCase(repository);
  }
}
