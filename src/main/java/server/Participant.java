package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Participant interface represents a remote object that acts as a participant in a two-phase commit protocol.
 */
public interface Participant extends Remote {

  /**
   * This method is used to prepare a transaction. It is called by the coordinator to propose a transaction to this participant.
   *
   * @param transaction The transaction to be prepared. It is a string representation of the transaction details.
   * @return A boolean indicating whether the preparation was successful. True if the preparation was successful, false otherwise.
   * @throws RemoteException If a remote communication error occurs.
   */
  boolean prepare(String transaction) throws RemoteException;

  /**
   * This method is used to commit a transaction. It is called by the coordinator to commit the transaction at this participant.
   *
   * @return An integer representing the status of the transaction commit.
   * @throws RemoteException If a remote communication error occurs.
   */
  int commit() throws RemoteException;
}
