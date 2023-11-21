package server;

import common.Constants;
import common.Service;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * The ParticipantImpl class is an implementation of the Participant and Service interfaces.
 * It extends the UnicastRemoteObject to provide remote call capabilities.
 * This class represents a participant in a two-phase commit protocol.
 */
public class ParticipantImpl extends UnicastRemoteObject implements Participant, Service {


  /**
   * Constructs a new ParticipantImpl object, initializing the map and getting the
   * coordinator from its registry.
   *
   * @param coordinatorHost The host name of the coordinator.
   * @param coordinatorPort The port number of the coordinator.
   * @param serverPort The port number for this server.
   * @throws RemoteException If a remote communication error occurs.
   */
  private static final Logger LOGGER = Logger.getLogger(ParticipantImpl.class.getName());
  private Map<String, String> map;
  private String transaction;
  private Coordinator coordinator;
  private int serverPort;

  /**
   * Constructs a new ParticipantImpl object, initializing the  map and getting the
   * coordinator from its registry.
   *
   * @throws RemoteException If a remote communication error occurs.
   */
  public ParticipantImpl(String coordinatorHost, int coordinatorPort, int serverPort) throws RemoteException {
    try {
      Registry registry = LocateRegistry.getRegistry(coordinatorHost, coordinatorPort);
      coordinator = (Coordinator) registry.lookup("Coordinator");
    } catch (Exception e) {
      throw new RemoteException("Unable to connect to coordinator", e);
    }
    map = new HashMap<>();
    this.serverPort = serverPort;
  }

  @Override
  public synchronized boolean prepare(String transaction) throws RemoteException {
    this.transaction = transaction;
    logMessage("preparing transaction: " + transaction);
    String[] parameters = transaction.split(" ");
    switch (parameters[0]) {
      case "put":
        if (map.containsKey(parameters[1])) {
          logMessage("Key " + parameters[1] + " is already present");
          return false;
        }
        break;
      case "post":
        break;
      case "delete":
        if (!map.containsKey(parameters[1])) {
          logMessage("Key " + parameters[1] + " not present");
          return false;
        }
        break;
      default:
        logMessage("Invalid command!!!");
        return false;
    }
    return true;
  }

  @Override
  public synchronized int commit() throws RemoteException {
    String[] parameters = transaction.split(" ");
    switch (parameters[0]) {
      case "put":
      case "post":
        map.put(parameters[1], parameters[2]);
        logMessage("Committed transaction: " + transaction);
        logMessage("Added key: " + parameters[1]);
        break;
      case "delete":
        map.remove(parameters[1]);
        logMessage("Committed transaction: " + transaction);
        logMessage("Removed key: " + parameters[1]);
        break;
      default:
        logMessage("Invalid command!!!");
    }
    return Constants.STATUS_SUCCESS;
  }

  @Override
  public int put(String key, String value) throws RemoteException {
    this.transaction = "put " + key + " " + value;
    logMessage("Received request: " + transaction);
    return coordinator.prepareTransaction(this.transaction);
  }

  @Override
  public int post(String key, String value) throws RemoteException {
    this.transaction = "post " + key + " " + value;
    logMessage("Received request: " + transaction);
    return coordinator.prepareTransaction(this.transaction);
  }

  @Override
  public String get(String key) throws RemoteException {
    this.transaction = "get " + key;
    logMessage("Received request: " + transaction);
    String value;
    if (map.containsKey(key)) {
      value = map.get(key);
    } else {
      value = "Key " + key + " not found!!!";
    }
    logMessage("Sending value: " + value);
    return value;
  }

  @Override
  public int delete(String key) throws RemoteException {
    this.transaction = "delete " + key;
    logMessage("Received request: " + transaction);
    return coordinator.prepareTransaction(this.transaction);
  }

  @Override
  public synchronized int getMapSize() throws RemoteException {
    return map.size();
  }

  private void logMessage(String message) {
    LOGGER.info("Participant-" + serverPort + ": " + message);
  }
}
