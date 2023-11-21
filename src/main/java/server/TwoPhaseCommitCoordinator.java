package server;

import common.Constants;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The TwoPhaseCommitCoordinator class is an implementation of the Coordinator interface.
 * It extends the UnicastRemoteObject to provide remote call capabilities.
 * This class represents a coordinator in a two-phase commit protocol.
 */
public class TwoPhaseCommitCoordinator extends UnicastRemoteObject implements Coordinator {

  private static final Logger LOGGER = Logger.getLogger(TwoPhaseCommitCoordinator.class.getName());
  private List<Participant> participants;

  /**
   * Constructs a new TwoPhaseCommitCoordinator object, initializing the participants list.
   *
   * @throws RemoteException If a remote communication error occurs.
   */
  public TwoPhaseCommitCoordinator() throws RemoteException {
    participants = new ArrayList<>();
  }

  /**
   * This method is used to set the participants for the two-phase commit protocol.
   * It connects to each participant using their host and port, and adds them to the participants list.
   *
   * @param participantHosts A list of host names for the participants.
   * @param participantPorts A list of port numbers for the participants.
   * @throws RemoteException If a remote communication error occurs.
   */
  public void setParticipants(List<String> participantHosts, List<Integer> participantPorts) throws RemoteException {
    for (int i = 0; i < participantHosts.size(); i++) {
      try {
        Registry registry = LocateRegistry.getRegistry(participantHosts.get(i), participantPorts.get(i));
        Participant participant = (Participant) registry.lookup("Server");
        participants.add(participant);
      } catch (Exception e) {
        throw new RemoteException("Unable to connect to participant", e);
      }
    }
  }

  @Override
  public int prepareTransaction(String transaction) throws RemoteException {
    for (Participant participant : participants) {
      if (!participant.prepare(transaction)) {
        LOGGER.info("Transaction: " + transaction + " aborted!!!");
        return Constants.STATUS_ABORTED;
      }
    }
    for (Participant participant : participants) {
      participant.commit();
    }
    return Constants.STATUS_SUCCESS;
  }
}
