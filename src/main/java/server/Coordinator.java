package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Coordinator interface represents a remote object that acts as a coordinator in a two-phase commit protocol.
 */
public interface Coordinator extends Remote {


  /**
   * This method is used to prepare a transaction. It is called by the coordinator to propose a transaction to the participants.
   *
   * @param transaction The transaction to be prepared. It is a string representation of the transaction details.
   * @return An integer representing the status of the transaction preparation. The meaning of the status codes should be defined in the implementation.
   * @throws RemoteException If a remote communication error occurs.
   */
  int prepareTransaction(String transaction) throws RemoteException;
}
