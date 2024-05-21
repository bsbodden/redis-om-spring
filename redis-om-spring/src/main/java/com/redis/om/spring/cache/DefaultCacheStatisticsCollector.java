package com.redis.om.spring.cache;

import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCacheStatisticsCollector implements CacheStatisticsCollector {

  private final Map<String, MutableCacheStatistics> stats = new ConcurrentHashMap<>();

  @Override
  public void incPuts(String cacheName) {
    statsFor(cacheName).incPuts();
  }

  @Override
  public void incGets(String cacheName) {
    statsFor(cacheName).incGets();
  }

  @Override
  public void incHits(String cacheName) {
    statsFor(cacheName).incHits();
  }

  @Override
  public void incMisses(String cacheName) {
    statsFor(cacheName).incMisses();
  }

  @Override
  public void incDeletesBy(String cacheName, int value) {
    statsFor(cacheName).incDeletes(value);
  }

  @Override
  public void incLockTime(String name, long durationNS) {
    statsFor(name).incLockWaitTime(durationNS);
  }

  @Override
  public void reset(String cacheName) {
    statsFor(cacheName).reset();
  }

  @Override
  public CacheStatistics getCacheStatistics(String cacheName) {
    return statsFor(cacheName).captureSnapshot();
  }

  private MutableCacheStatistics statsFor(String cacheName) {
    return stats.computeIfAbsent(cacheName, MutableCacheStatistics::new);
  }
}

