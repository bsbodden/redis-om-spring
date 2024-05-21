package com.redis.om.spring.cache;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class RedisJSONCacheWriter implements RedisCacheWriter {
  private final RedisOperations<String, ?> redisOperations;
  private final RedisModulesOperations<String> redisModulesOperations;
  private final Gson2JsonRedisSerializer serializer;
  private final JSONOperations<String> jsonOperations;
  private final CacheStatisticsCollector statistics;

  public RedisJSONCacheWriter(RedisOperations<?, ?> redisOperations, RedisModulesOperations<?> redisModulesOperations,
    GsonBuilder gsonBuilder, CacheStatisticsCollector cacheStatisticsCollector) {
    this.redisOperations = (RedisOperations<String, ?>) redisOperations;
    this.redisModulesOperations = (RedisModulesOperations<String>) redisModulesOperations;
    this.jsonOperations = (JSONOperations<String>) redisModulesOperations.opsForJSON();
    this.serializer = new Gson2JsonRedisSerializer(gsonBuilder);
    this.statistics = cacheStatisticsCollector;
  }

  @Override
  public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
    String jsonKey = new String(key);
    Object deserialized = serializer.deserialize(value);
    jsonOperations.set(jsonKey, deserialized);
    if (ttl != null) {
      redisOperations.expire(jsonKey, ttl);
    }
  }

  @Override
  public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
    return null;
  }

  @Override
  public byte[] get(String name, byte[] key) {
    String jsonKey = new String(key);
    Object value = jsonOperations.get(jsonKey);
    return value != null ? serializer.serialize(value) : null;
  }

  @Override
  public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
    return null;
  }

  @Override
  public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
    String jsonKey = new String(key);
    JSONOperations<String> jsonOperations = (JSONOperations<String>) redisModulesOperations.opsForJSON();
    Object deserialized = serializer.deserialize(value);
    Object existingValue = jsonOperations.get(jsonKey);
    if (existingValue == null) {
      jsonOperations.set(jsonKey, deserialized);
      if (ttl != null) {
        redisOperations.expire(jsonKey, ttl);
      }
      return null;
    }
    return serializer.serialize(existingValue);
  }

  @Override
  public void remove(String name, byte[] key) {
    String jsonKey = new String(key);
    redisOperations.delete(jsonKey);
  }

  @Override
  public void clean(String name, byte[] pattern) {
    String patternString = new String(pattern);
    redisOperations.keys(patternString).forEach(redisOperations::delete);
  }

  @Override
  public void clearStatistics(String name) {
    statistics.reset(name);
  }

  @Override
  public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
    return null;
  }

  @Override
  public CacheStatistics getCacheStatistics(String cacheName) {
    return statistics.getCacheStatistics(cacheName);
  }
}
