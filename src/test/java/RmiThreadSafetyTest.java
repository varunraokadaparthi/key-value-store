import common.Service;

import org.junit.Test;

import java.rmi.Naming;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class RmiThreadSafetyTest {
  @Test
  public void testThreadSafety() {

    int numThreads = 100;
    CountDownLatch latch = new CountDownLatch(numThreads);
    AtomicInteger addPostfix = new AtomicInteger(0);
    AtomicInteger deletePostfix = new AtomicInteger(0);
    AtomicInteger successfulAdds = new AtomicInteger(0);
    AtomicInteger successfulDeletes = new AtomicInteger(0);


    Runnable addTask = () -> {
      try {
        Service obj = (Service) Naming.lookup("rmi://localhost:1099" + "/server");
        if (obj.put("key" + addPostfix.incrementAndGet(), "value") == 0) {
          successfulAdds.incrementAndGet();
        }
      } catch (Exception e) {
        new RuntimeException(e);
      }
      latch.countDown();
    };

    Runnable deleteTask = () -> {
      try {
        Service obj = (Service) Naming.lookup("rmi://localhost:1099" + "/server");
        if (obj.delete("key" + deletePostfix.incrementAndGet()) == 0) {
          successfulDeletes.incrementAndGet();
        }
      } catch (Exception e) {
        new RuntimeException(e);
      }
      latch.countDown();
    };

    for (int i = 0; i < numThreads; i++) {
      if (i % 2 == 0) {
        new Thread(addTask).start();
      } else {
        new Thread(deleteTask).start();
      }
    }


    try {
      latch.await();
      Service obj = (Service) Naming.lookup("rmi://localhost:1099/server");
      int expectedSize = successfulAdds.get() - successfulDeletes.get();
      int actualSize = obj.getMapSize();
      assertEquals(expectedSize, actualSize);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
