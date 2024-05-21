package com.redis.om.spring.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

public class Gson2JsonRedisSerializer implements RedisSerializer<Object> {
  private final Gson gson;

  public Gson2JsonRedisSerializer(GsonBuilder gsonBuilder) {
    this.gson = gsonBuilder.create();
  }

  @Override
  public byte[] serialize(@Nullable Object value) throws SerializationException {
    if (value == null) {
      return new byte[0];
    }
    return gson.toJson(value).getBytes();
  }

  @Override
  public Object deserialize(@Nullable byte[] bytes) throws SerializationException {
    if (bytes == null || bytes.length == 0) {
      return null;
    }
    return gson.fromJson(new String(bytes), Object.class);
  }
}
