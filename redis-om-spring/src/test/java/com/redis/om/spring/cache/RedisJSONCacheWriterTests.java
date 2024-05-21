package com.redis.om.spring.cache;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.AbstractBaseDocumentTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.core.RedisOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RedisJSONCacheWriterTests extends AbstractBaseDocumentTest {

  private RedisOperations<String, Object> redisOperations;
  private CacheStatisticsCollector cacheStatisticsCollector;

  private RedisJSONCacheWriter cacheWriter;
  private Gson2JsonRedisSerializer serializer;

  @BeforeEach
  void setUp() {
    cacheStatisticsCollector = new DefaultCacheStatisticsCollector();

    GsonBuilder gsonBuilder = new GsonBuilder();
    serializer = new Gson2JsonRedisSerializer(gsonBuilder);
    cacheWriter = new RedisJSONCacheWriter(redisOperations, modulesOperations, gsonBuilder, cacheStatisticsCollector);
  }

  @Test
  void putShouldStoreSerializedValueInRedis() {
    String cacheName = "testCache";
    byte[] key = "testKey".getBytes();
    byte[] value = serializer.serialize("testValue");
    Duration ttl = Duration.ofSeconds(60);

    cacheWriter.put(cacheName, key, value, ttl);

    Object result = modulesOperations.opsForJSON().get("testKey");
    assertThat(result).isEqualTo("testValue");
    assertThat(redisOperations.getExpire("testKey")).isGreaterThan(50).isLessThanOrEqualTo(60);
  }

  @Test
  void getShouldReturnDeserializedValueFromRedis() {
    String cacheName = "testCache";
    byte[] key = "testKey".getBytes();
    String expectedValue = "testValue";

    modulesOperations.opsForJSON().set("testKey", expectedValue);

    byte[] result = cacheWriter.get(cacheName, key);

    assertThat(serializer.deserialize(result)).isEqualTo(expectedValue);
  }

  @Test
  void putIfAbsentShouldStoreSerializedValueIfKeyDoesNotExist() {
    String cacheName = "testCache";
    byte[] key = "testKey".getBytes();
    byte[] value = serializer.serialize("testValue");
    Duration ttl = Duration.ofSeconds(60);

    cacheWriter.putIfAbsent(cacheName, key, value, ttl);

    Object result = modulesOperations.opsForJSON().get("testKey");
    assertThat(result).isEqualTo("testValue");
    assertThat(redisOperations.getExpire("testKey")).isGreaterThan(50).isLessThanOrEqualTo(60);
  }

  @Test
  void putIfAbsentShouldReturnExistingValueIfKeyExists() {
    String cacheName = "testCache";
    byte[] key = "testKey".getBytes();
    byte[] value = serializer.serialize("testValue");
    String existingValue = "existingValue";

    modulesOperations.opsForJSON().set("testKey", existingValue);

    byte[] result = cacheWriter.putIfAbsent(cacheName, key, value, null);

    assertThat(serializer.deserialize(result)).isEqualTo(existingValue);
    assertThat(modulesOperations.opsForJSON().get("testKey")).isEqualTo(existingValue);
  }

  @Test
  void removeShouldDeleteKeyFromRedis() {
    String cacheName = "testCache";
    byte[] key = "testKey".getBytes();

    modulesOperations.opsForJSON().set("testKey", "testValue");

    cacheWriter.remove(cacheName, key);

    assertThat(redisOperations.hasKey("testKey")).isFalse();
  }

  @Test
  void cleanShouldDeleteMatchingKeysFromRedis() {
    String cacheName = "testCache";
    byte[] pattern = "test*".getBytes();

    modulesOperations.opsForJSON().set("testKey1", "value1");
    modulesOperations.opsForJSON().set("testKey2", "value2");
    modulesOperations.opsForJSON().set("otherKey", "value3");

    cacheWriter.clean(cacheName, pattern);

    assertThat(redisOperations.hasKey("testKey1")).isFalse();
    assertThat(redisOperations.hasKey("testKey2")).isFalse();
    assertThat(redisOperations.hasKey("otherKey")).isTrue();
  }

  @Test
  void clearStatisticsShouldResetStatisticsForCache() {
    String cacheName = "testCache";

    cacheStatisticsCollector.incPuts(cacheName);
    cacheStatisticsCollector.incGets(cacheName);

    cacheWriter.clearStatistics(cacheName);

    CacheStatistics statistics = cacheStatisticsCollector.getCacheStatistics(cacheName);
    assertThat(statistics.getPuts()).isZero();
    assertThat(statistics.getGets()).isZero();
  }

  @Test
  void getCacheStatisticsShouldReturnStatisticsForCache() {
    String cacheName = "testCache";

    cacheStatisticsCollector.incPuts(cacheName);
    cacheStatisticsCollector.incGets(cacheName);

    CacheStatistics result = cacheWriter.getCacheStatistics(cacheName);

    assertThat(result.getPuts()).isEqualTo(1);
    assertThat(result.getGets()).isEqualTo(1);
  }
}
