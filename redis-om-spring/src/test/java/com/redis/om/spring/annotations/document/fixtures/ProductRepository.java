package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.Optional;

public interface ProductRepository extends RedisDocumentRepository<Product, String> {
  Optional<Product> findFirstByName(String name);
}
