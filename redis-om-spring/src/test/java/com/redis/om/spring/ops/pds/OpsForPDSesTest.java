package com.redis.om.spring.ops.pds;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.ROMSOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("SpellCheckingInspection") class OpsForPDSesTest extends AbstractBaseDocumentTest {
  @Autowired
  ROMSOperations<String, ?> modulesOperations;
  
  @Test
  void testBasicBloomOperations() {
    BloomOperations<String> bloom = modulesOperations.opsForBloom();

    assertNotNull(bloom);
  }
  
  @Test
  void testBasicCMSOperations() {
    CountMinSketchOperations<String> cms = modulesOperations.opsForCountMinSketch();

    assertNotNull(cms);
  }
  
  @Test
  void testTopKOperations() {
    TopKOperations<String> topk = modulesOperations.opsForTopK();
    
    assertNotNull(topk);
  }
  
  @Test
  void testCuckooKOperations() {
    CuckooFilterOperations<String> cuckoo = modulesOperations.opsForCuckooFilter();
    
    assertNotNull(cuckoo);
  }
}
