package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Service interface defines a set of remote methods for key-value service operations.
 * These methods allow clients to interact with a key-value service over RMI (Remote Method Invocation).
 */
public interface KVStoreInterface extends Remote {

  /**
   * Inserts a key-value pair into the service.
   *
   * @param key   The key to insert.
   * @param value The value associated with the key.
   * @return An integer indicating the result of the operation.
   * @throws RemoteException If a remote communication error occurs.
   */
  boolean put(String key, String value) throws RemoteException;

  /**
   * Retrieves the value associated with a given key from the service.
   *
   * @param key The key to retrieve.
   * @return The value associated with the key, or null if the key is not found.
   * @throws RemoteException If a remote communication error occurs.
   */
  String get(String key) throws RemoteException;

  /**
   * Deletes a key-value pair from the service based on the provided key.
   *
   * @param key The key to delete.
   * @return An integer indicating the result of the operation.
   * @throws RemoteException If a remote communication error occurs.
   */
  boolean delete(String key) throws RemoteException;
}
