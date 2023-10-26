import common.Service;

import org.junit.Test;

import java.rmi.Naming;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * The `RmiThreadSafetyTest` class defines a JUnit test case for evaluating the thread safety
 * of a remote key-value service accessed through RMI (Remote Method Invocation).
 */
public class RmiThreadSafetyTest {

  /**
   * Do check the server port before running.
   */
  @Test
  public void testThreadSafety() {
    String registryURI = "rmi://localhost:9090/server";
    int numThreads = 100;
    CountDownLatch latch = new CountDownLatch(numThreads);
    AtomicInteger addPostfix = new AtomicInteger(0);
    AtomicInteger deletePostfix = new AtomicInteger(0);
    AtomicInteger successfulAdds = new AtomicInteger(0);
    AtomicInteger successfulDeletes = new AtomicInteger(0);


    Runnable addTask = () -> {
      try {
        Service obj = (Service) Naming.lookup(registryURI);
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
        Service obj = (Service) Naming.lookup(registryURI);
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
      Service obj = (Service) Naming.lookup(registryURI);
      int expectedSize = successfulAdds.get() - successfulDeletes.get();
      int actualSize = obj.getMapSize();
      assertEquals(expectedSize, actualSize);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
