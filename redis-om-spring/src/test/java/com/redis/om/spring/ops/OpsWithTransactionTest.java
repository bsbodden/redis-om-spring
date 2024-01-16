package com.redis.om.spring.ops;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.json.JSONOperations;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.testcontainers.containers.output.OutputFrame.OutputType;
import org.testcontainers.containers.output.ToStringConsumer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpsWithTransactionTest extends AbstractBaseDocumentTest {

  public static class MyDoc {
    public String text;
    public boolean read;

    public MyDoc() {
      this.text = "Stop, drop and roll";
    }
  }

  @Test
  void testJSONClient() {
    // BAD
    //    1705255543.652092 [0 192.168.65.1:18575] "WATCH" "testkey"
    //    1705255543.658296 [0 192.168.65.1:18546] "JSON.SET" "testkey" "$" "{\"text\":\"Stop, drop and roll\",\"read\":false}"
    //    1705255543.658354 [0 ] "type" "testkey"
    //    1705255543.671191 [0 192.168.65.1:18575] "MULTI"
    //    1705255543.671568 [0 192.168.65.1:18575] "EXEC"
    //    1705255543.671821 [0 192.168.65.1:18575] "UNWATCH"

    // GOOD
//    1705255800.898029 [0 192.168.65.1:18941] "WATCH" "testkey"
//    1705255800.945708 [0 192.168.65.1:18941] "MULTI"
//    1705255800.946297 [0 192.168.65.1:18941] "SET" "testkey" "Ze Value"
//    1705255800.946320 [0 192.168.65.1:18941] "EXEC"
//    1705255800.946426 [0 ] "type" "testkey"
//    1705255800.947415 [0 192.168.65.1:18941] "UNWATCH"
    ToStringConsumer toStringConsumer = new ToStringConsumer();
    REDIS.followOutput(toStringConsumer, OutputType.STDOUT);
    JSONOperations<String,?> ops = modulesOperations.opsForJSON();

    //execute a transaction
    List<Object> txResults = modulesOperations.execute(new SessionCallback<List<Object>>() {
      public List<Object> execute(RedisOperations operations) throws DataAccessException {
        ROMSOperations<String,Object> ops = (ROMSOperations)operations;
        ops.watch("testkey");
        ops.multi();
        ops.opsForValue().set("testkey2", "Ze Value");
        ops.opsForJSON().set("testkey", new MyDoc());
        return ops.exec();
      }
    });
    System.out.println("Number of items added to set: " + txResults.get(0));
    String utf8String = toStringConsumer.toUtf8String();
    System.out.println("Redis Output:");
    System.out.println(utf8String);

  }
}
