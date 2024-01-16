package com.redis.om.spring.ops;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import com.redis.om.spring.ops.pds.TopKOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;

public interface ROMSOperations<K, V> extends RedisOperations<K, V> {

    JSONOperations<K, V> opsForJSON();

    SearchOperations<K> opsForSearch(K index);

    BloomOperations<K> opsForBloom();

    CountMinSketchOperations<K> opsForCountMinSketch();

    CuckooFilterOperations<K> opsForCuckooFilter();

    TopKOperations<K> opsForTopK();

    GsonBuilder getGsonBuilder();

    RedisModulesClient getModulesClient();

    RedisTemplate<K, V> getTemplate();
}
