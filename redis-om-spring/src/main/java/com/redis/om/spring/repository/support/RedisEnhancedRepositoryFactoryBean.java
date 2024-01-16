package com.redis.om.spring.repository.support;

import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.ops.ROMSOperations;
import com.redis.om.spring.repository.query.RedisEnhancedQuery;
import com.redis.om.spring.vectorize.FeatureExtractor;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.config.QueryCreatorType;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.redis.repository.support.RedisRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class RedisEnhancedRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends RepositoryFactoryBeanSupport<T, S, ID> {

  private @Nullable KeyValueOperations operations;
  private @Nullable ROMSOperations<String,?> rmo;
  private @Nullable RediSearchIndexer indexer;
  private @Nullable Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
  private @Nullable Class<? extends RepositoryQuery> repositoryQueryType;
  private @Nullable FeatureExtractor featureExtractor;

  private RedisOMProperties properties;

  /**
   * Creates a new {@link RedisRepositoryFactoryBean} for the given repository
   * interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   * @param rmo must not be {@literal null}.
   * @param indexer must not be {@literal null}.
   */
  public RedisEnhancedRepositoryFactoryBean( //
      Class<? extends T> repositoryInterface, //
      ROMSOperations<?,?> rmo, //
      RediSearchIndexer indexer, //
      FeatureExtractor featureExtractor, //
      RedisOMProperties properties
  ) {
    super(repositoryInterface);
    setRedisModulesOperations(rmo);
    setKeyspaceToIndexMap(indexer);
    setFeatureExtractor(featureExtractor);
    setRedisOMSpringProperties(properties);
  }

  private void setFeatureExtractor(FeatureExtractor featureExtractor) {
    Assert.notNull(rmo, "FeatureExtractor must not be null!");

    this.featureExtractor = featureExtractor;
  }

  /**
   * Configures the {@link KeyValueOperations} to be used for the repositories.
   *
   * @param operations must not be {@literal null}.
   */
  public void setKeyValueOperations(KeyValueOperations operations) {

    Assert.notNull(operations, "KeyValueOperations must not be null!");

    this.operations = operations;
  }

  /**
   * Configures the {@link ROMSOperations} to be used for the
   * repositories.
   *
   * @param rmo must not be {@literal null}.
   */
  @SuppressWarnings("unchecked")
  public void setRedisModulesOperations(ROMSOperations<?,?> rmo) {
    Assert.notNull(rmo, "ROMSOperations must not be null!");

    this.rmo = (ROMSOperations<String,?>) rmo;
  }

  public void setRedisOMSpringProperties(RedisOMProperties properties) {
    Assert.notNull(properties, "RedisOMSpringProperties must not be null!");
    this.properties = properties;
  }

  /* (non-Javadoc)
   * 
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
   * #setMappingContext(org.springframework.data.mapping.context.
   * MappingContext) */
  @Override
  public void setMappingContext(MappingContext<?, ?> mappingContext) {
    super.setMappingContext(mappingContext);
  }
  
  public void setKeyspaceToIndexMap(RediSearchIndexer keyspaceToIndexMap) { 
    this.indexer = keyspaceToIndexMap;
  }

  /**
   * Configures the {@link QueryCreatorType} to be used.
   *
   * @param queryCreator must not be {@literal null}.
   */
  public void setQueryCreator(Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

    Assert.notNull(queryCreator, "Query creator type must not be null!");

    this.queryCreator = queryCreator;
  }

  /**
   * Configures the {@link RepositoryQuery} type to be created.
   *
   * @param repositoryQueryType must not be {@literal null}.
   * @since 1.1
   */
  public void setQueryType(Class<? extends RepositoryQuery> repositoryQueryType) {
    Assert.notNull(queryCreator, "Query creator type must not be null!");
    this.repositoryQueryType = repositoryQueryType;
  }

  /* (non-Javadoc)
   * 
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
   * #createRepositoryFactory() */
  @Override
  protected final RepositoryFactorySupport createRepositoryFactory() {
    return createRepositoryFactory(operations, queryCreator, repositoryQueryType);
  }

  /**
   * Create the repository factory to be used to create repositories.
   *
   * @param operations          will never be {@literal null}.
   * @param queryCreator        will never be {@literal null}.
   * @param repositoryQueryType will never be {@literal null}.
   * @return must not be {@literal null}.
   */
  protected RedisEnhancedRepositoryFactory createRepositoryFactory( //
      KeyValueOperations operations, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      Class<? extends RepositoryQuery> repositoryQueryType //
  ) {
    return new RedisEnhancedRepositoryFactory(operations, rmo, indexer, featureExtractor, queryCreator, RedisEnhancedQuery.class, properties);
  }

  /* (non-Javadoc)
   * 
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
   * #afterPropertiesSet() */
  @Override
  public void afterPropertiesSet() {
    Assert.notNull(operations, "KeyValueOperations must not be null!");
    Assert.notNull(rmo, "ROMSOperations must not be null!");
    Assert.notNull(queryCreator, "Query creator must not be null!");
    Assert.notNull(repositoryQueryType, "RepositoryQueryType must not be null!");
    Assert.notNull(indexer, "RediSearchIndexer type must not be null");
    Assert.notNull(featureExtractor, "FeatureExtractor type must not be null!");

    super.afterPropertiesSet();
  }

}
