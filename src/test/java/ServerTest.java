import common.Service;

import org.junit.Test;

import java.rmi.RemoteException;

import server.Server;

import static org.junit.Assert.assertEquals;

public class ServerTest {

  @Test
  public void testPut() {
    String key = "name";
    String value = "john";
    try {
      Service server = new Server();
      server.put(key, value);
      assertEquals(server.get(key), value);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGet() {
    String key = "age";
    String value = "10";
    try {
      Service server = new Server();
      server.put(key, value);
      assertEquals(server.get(key), value);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testPost() {
    String key = "college";
    String value = "khoury";
    try {
      Service server = new Server();
      server.post(key, value);
      assertEquals(server.get(key), value);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testDelete() {
    String key = "college";
    String value = "khoury";
    try {
      Service server = new Server();
      server.post(key, value);
      server.delete(key);
      assertEquals(server.get(key), "Key not found!!!");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }
}
