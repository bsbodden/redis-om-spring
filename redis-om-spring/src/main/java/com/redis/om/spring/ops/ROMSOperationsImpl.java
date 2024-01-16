package com.redis.om.spring.ops;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.json.JSONOperationsImpl;
import com.redis.om.spring.ops.pds.*;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.ops.search.SearchOperationsImpl;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.RedisClientInfo;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public record ROMSOperationsImpl<K,V>(RedisModulesClient client, RedisTemplate<K,V> template, GsonBuilder gsonBuilder) implements
  ROMSOperations<K,V> {

  public JSONOperations<K,V> opsForJSON() {
    return new JSONOperationsImpl<>(client, gsonBuilder, template);
  }

  public SearchOperations<K> opsForSearch(K index) {
    return new SearchOperationsImpl<>(index, client, template);
  }

  public BloomOperations<K> opsForBloom() {
    return new BloomOperationsImpl<>(client);
  }

  public CountMinSketchOperations<K> opsForCountMinSketch() {
    return new CountMinSketchOperationsImpl<>(client);
  }

  @Override
  public CuckooFilterOperations<K> opsForCuckooFilter() {
    return new CuckooFilterOperationsImpl<>(client);
  }

  public TopKOperations<K> opsForTopK() {
    return new TopKOperationsImpl<>(client);
  }

  @Override
  public GsonBuilder getGsonBuilder() {
    return this.gsonBuilder;
  }

  @Override
  public RedisModulesClient getModulesClient() {
    return client;
  }

  @Override
  public RedisTemplate<K, V> getTemplate() {
    return template;
  }

  @Override
  public <T> T execute(RedisCallback<T> action) {
    return template.execute(action);
  }

  @Override
  public <T> T execute(SessionCallback<T> session) {
    //return template.execute(session);
    Assert.notNull(session, "Callback object must not be null");

    RedisConnectionFactory factory = template.getRequiredConnectionFactory();
    // bind connection
    RedisConnectionUtils.bindConnection(factory, true);
    try {
      return session.execute(this);
    } finally {
      RedisConnectionUtils.unbindConnection(factory);
    }
  }

  @Override
  public List<Object> executePipelined(RedisCallback<?> action) {
    return template.executePipelined(action);
  }

  @Override
  public List<Object> executePipelined(RedisCallback<?> action, RedisSerializer<?> resultSerializer) {
    return template.executePipelined(action, resultSerializer);
  }

  @Override
  public List<Object> executePipelined(SessionCallback<?> session) {
    return template.executePipelined(session);
  }

  @Override
  public List<Object> executePipelined(SessionCallback<?> session, RedisSerializer<?> resultSerializer) {
    return template.executePipelined(session, resultSerializer);
  }

  @Override
  public <T> T execute(RedisScript<T> script, List<K> keys, Object... args) {
    return template.execute(script, keys, args);
  }

  @Override
  public <T> T execute(RedisScript<T> script, RedisSerializer<?> argsSerializer, RedisSerializer<T> resultSerializer,
    List<K> keys, Object... args) {
    return template.execute(script, argsSerializer, resultSerializer, keys, args);
  }

  @Override
  public <T extends Closeable> T executeWithStickyConnection(RedisCallback<T> callback) {
    return template.executeWithStickyConnection(callback);
  }

  @Override
  public Boolean copy(K sourceKey, K targetKey, boolean replace) {
    return template.copy(sourceKey, targetKey, replace);
  }

  @Override
  public Boolean hasKey(K key) {
    return template.hasKey(key);
  }

  @Override
  public Long countExistingKeys(Collection<K> keys) {
    return template.countExistingKeys(keys);
  }

  @Override
  public Boolean delete(K key) {
    return template.delete(key);
  }

  @Override
  public Long delete(Collection<K> keys) {
    return template.delete(keys);
  }

  @Override
  public Boolean unlink(K key) {
    return template.unlink(key);
  }

  @Override
  public Long unlink(Collection<K> keys) {
    return template.unlink(keys);
  }

  @Override
  public DataType type(K key) {
    return template.type(key);
  }

  @Override
  public Set<K> keys(K pattern) {
    return template.keys(pattern);
  }

  @Override
  public Cursor<K> scan(ScanOptions options) {
    return template.scan(options);
  }

  @Override
  public K randomKey() {
    return template.randomKey();
  }

  @Override
  public void rename(K oldKey, K newKey) {
    template.rename(oldKey, newKey);
  }

  @Override
  public Boolean renameIfAbsent(K oldKey, K newKey) {
    return template.renameIfAbsent(oldKey, newKey);
  }

  @Override
  public Boolean expire(K key, long timeout, TimeUnit unit) {
    return template.expire(key, timeout, unit);
  }

  @Override
  public Boolean expireAt(K key, Date date) {
    return template.expireAt(key, date);
  }

  @Override
  public Boolean persist(K key) {
    return template.persist(key);
  }

  @Override
  public Long getExpire(K key) {
    return template.getExpire(key);
  }

  @Override
  public Long getExpire(K key, TimeUnit timeUnit) {
    return template.getExpire(key, timeUnit);
  }

  @Override
  public Boolean move(K key, int dbIndex) {
    return template.move(key, dbIndex);
  }

  @Override
  public byte[] dump(K key) {
    return template.dump(key);
  }

  @Override
  public void restore(K key, byte[] value, long timeToLive, TimeUnit unit, boolean replace) {
    template.restore(key, value, timeToLive, unit, replace);
  }

  @Override
  public List<V> sort(SortQuery<K> query) {
    return template.sort(query);
  }

  @Override
  public <T> List<T> sort(SortQuery<K> query, RedisSerializer<T> resultSerializer) {
    return template.sort(query, resultSerializer);
  }

  @Override
  public <T> List<T> sort(SortQuery<K> query, BulkMapper<T, V> bulkMapper) {
    return template.sort(query, bulkMapper);
  }

  @Override
  public <T, S> List<T> sort(SortQuery<K> query, BulkMapper<T, S> bulkMapper, RedisSerializer<S> resultSerializer) {
    return template.sort(query, bulkMapper, resultSerializer);
  }

  @Override
  public Long sort(SortQuery<K> query, K storeKey) {
    return template.sort(query, storeKey);
  }

  @Override
  public void watch(K key) {
    template.watch(key);
  }

  @Override
  public void watch(Collection<K> keys) {
    template.watch(keys);
  }

  @Override
  public void unwatch() {
    template.unwatch();
  }

  @Override
  public void multi() {
    template.multi();
  }

  @Override
  public void discard() {
    template.discard();
  }

  @Override
  public List<Object> exec() {
    return template.exec();
  }

  @Override
  public List<Object> exec(RedisSerializer<?> valueSerializer) {
    return template.exec(valueSerializer);
  }

  @Override
  public List<RedisClientInfo> getClientList() {
    return template.getClientList();
  }

  @Override
  public void killClient(String host, int port) {
    template.killClient(host, port);
  }

  @Override
  public void replicaOf(String host, int port) {
    template.replicaOf(host, port);
  }

  @Override
  public void replicaOfNoOne() {
    template.replicaOfNoOne();
  }

  @Override
  public Long convertAndSend(String destination, Object message) {
    return template.convertAndSend(destination, message);
  }

  @Override
  public ClusterOperations<K, V> opsForCluster() {
    return template.opsForCluster();
  }

  @Override
  public GeoOperations<K, V> opsForGeo() {
    return template.opsForGeo();
  }

  @Override
  public BoundGeoOperations<K, V> boundGeoOps(K key) {
    return template.boundGeoOps(key);
  }

  @Override
  public <HK, HV> HashOperations<K, HK, HV> opsForHash() {
    return template.opsForHash();
  }

  @Override
  public <HK, HV> BoundHashOperations<K, HK, HV> boundHashOps(K key) {
    return template.boundHashOps(key);
  }

  @Override
  public HyperLogLogOperations<K, V> opsForHyperLogLog() {
    return template.opsForHyperLogLog();
  }

  @Override
  public ListOperations<K, V> opsForList() {
    return template.opsForList();
  }

  @Override
  public BoundListOperations<K, V> boundListOps(K key) {
    return template.boundListOps(key);
  }

  @Override
  public SetOperations<K, V> opsForSet() {
    return template.opsForSet();
  }

  @Override
  public BoundSetOperations<K, V> boundSetOps(K key) {
    return template.boundSetOps(key);
  }

  @Override
  public <HK, HV> StreamOperations<K, HK, HV> opsForStream() {
    return template.opsForStream();
  }

  @Override
  public <HK, HV> StreamOperations<K, HK, HV> opsForStream(HashMapper<? super K, ? super HK, ? super HV> hashMapper) {
    return template.opsForStream(hashMapper);
  }

  @Override
  public <HK, HV> BoundStreamOperations<K, HK, HV> boundStreamOps(K key) {
    return template.boundStreamOps(key);
  }

  @Override
  public ValueOperations<K, V> opsForValue() {
    return template.opsForValue();
  }

  @Override
  public BoundValueOperations<K, V> boundValueOps(K key) {
    return template.boundValueOps(key);
  }

  @Override
  public ZSetOperations<K, V> opsForZSet() {
    return template.opsForZSet();
  }

  @Override
  public BoundZSetOperations<K, V> boundZSetOps(K key) {
    return template.boundZSetOps(key);
  }

  @Override
  public RedisSerializer<?> getKeySerializer() {
    return template.getKeySerializer();
  }

  @Override
  public RedisSerializer<?> getValueSerializer() {
    return template.getValueSerializer();
  }

  @Override
  public RedisSerializer<?> getHashKeySerializer() {
    return template.getHashKeySerializer();
  }

  @Override
  public RedisSerializer<?> getHashValueSerializer() {
    return template.getHashValueSerializer();
  }
}
