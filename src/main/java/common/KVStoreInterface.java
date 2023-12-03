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
  void put(String key, String value) throws RemoteException;

  /**
   * Updates the value associated with a key in the service.
   *
   * @param key   The key to update.
   * @param value The new value to associate with the key.
   * @return An integer indicating the result of the operation.
   * @throws RemoteException If a remote communication error occurs.
   */
  void post(String key, String value) throws RemoteException;

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
  void delete(String key) throws RemoteException;

  /**
   * Returns the current size of the key-value map in the service.
   *
   * @return The size of the key-value map.
   * @throws RemoteException If a remote communication error occurs.
   */
  int getMapSize() throws RemoteException;
}
