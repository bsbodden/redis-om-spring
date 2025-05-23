package com.redis.om.spring.fixtures.hash.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Data;

@RedisHash
@Data
public class HashProjectionPojo {

  @Id
  private String id;

  private String name;

  private String test;

  public HashProjectionPojo() {
    this.id = UUID.randomUUID().toString();
  }

}
