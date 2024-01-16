package com.redis.om.spring.search.stream;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.ops.ROMSOperations;

public class EntityStreamImpl implements EntityStream {

  private final ROMSOperations<String, ?> modulesOperations;
  private final GsonBuilder gsonBuilder;

  private final RediSearchIndexer indexer;

  @SuppressWarnings("unchecked")
  public EntityStreamImpl(ROMSOperations<?, ?> rmo, GsonBuilder gsonBuilder, RediSearchIndexer indexer) {
    this.modulesOperations = (ROMSOperations<String, ?>) rmo;
    this.gsonBuilder = gsonBuilder;
    this.indexer = indexer;
  }

  @Override
  public <E> SearchStream<E> of(Class<E> entityClass) {
    return new SearchStreamImpl<>(entityClass, modulesOperations, gsonBuilder, indexer);
  }

}
