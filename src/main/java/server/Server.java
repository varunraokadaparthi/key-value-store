package server;

import common.Service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * The `Server` class implements the `Service` interface and provides the server-side functionality
 * for a key-value service accessed through RMI (Remote Method Invocation).
 * It supports locking mechanism for concurrent operations' synchronization.
 */
public class Server implements Service {

  private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

  private Map<String, String> synchronizedMap;
  Map<String, String> map = new ConcurrentHashMap<>();
  private ReadWriteLock lock;
  private Lock readLock;
  private Lock writeLock;


  private static final String KEY_ADDED = "Key Added!!!";
  private static final String KEY_NOT_FOUND = "Key not found!!!";
  private static final String KEY_REMOVED = "Key successfully removed!!!";
  private static final String KEY_UPDATED = "Key successfully updated!!!";


  /**
   * Constructs a new `Server` object, initializing the synchronized map and locks.
   *
   * @throws RemoteException If a remote communication error occurs.
   */
  public Server() throws RemoteException {
    this.synchronizedMap = new HashMap<>();
    this.lock = new ReentrantReadWriteLock();
    this.readLock = lock.readLock();
    this.writeLock = lock.writeLock();
  }

  @Override
  public int put(String key, String value) throws RemoteException {
    logReceivedMessage("PUT " + key + " " + value);
    try {
      writeLock.lock();
      synchronizedMap.put(key, value);
    } finally {
      writeLock.unlock();
    }
    LOGGER.info("Added key: " + key + " value: " + value);
    return 0;
  }

  @Override
  public int post(String key, String value) throws RemoteException {
    logReceivedMessage("POST " + key + " " + value);
    try {
      writeLock.lock();
      synchronizedMap.put(key, value);
    } finally {
      writeLock.unlock();
    }
    LOGGER.info("Added key: " + key + " value: " + value);
    return 0;
  }

  @Override
  public String get(String key) throws RemoteException {
    logReceivedMessage("GET " + key);
    String message;
    if (this.containsKey(key)) {
      try {
        readLock.lock();
        message = synchronizedMap.get(key);
      } finally {
        readLock.unlock();
      }
    } else {
      message = KEY_NOT_FOUND;
    }
    if (message == KEY_NOT_FOUND) {
      LOGGER.info(key + " " + message);
    } else {
      LOGGER.info(message);
    }
    return message;
  }

  @Override
  public int delete(String key) throws RemoteException {
    logReceivedMessage("DELETE " + key);
    if (this.containsKey(key)) {
      try {
        readLock.lock();
        synchronizedMap.remove(key);
      } finally {
        readLock.unlock();
      }
      LOGGER.info(key + " " + KEY_REMOVED);
      return 0;
    } else {
      LOGGER.info(key + " " + KEY_NOT_FOUND);
      return -1;
    }
  }

  @Override
  public int getMapSize() {
    return this.synchronizedMap.size();
  }

  /**
   * Checks if the synchronized map contains the specified key.
   *
   * @param key The key to check for existence.
   * @return `true` if the key exists in the map, `false` otherwise.
   */
  private boolean containsKey(String key) {
    boolean exists;
    try {
      readLock.lock();
      exists = synchronizedMap.containsKey(key);
    } finally {
      readLock.unlock();
    }
    return exists;
  }

  /**
   * Logs the received message for the server.
   *
   * @param message The message to log as a received request.
   */
  private void logReceivedMessage(String message) {
    LOGGER.info("Received request: " + message);
  }
}
