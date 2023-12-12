package server;

import common.Constants;
import common.KVStoreInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Implementation of a Server class that represents a node in a Paxos distributed consensus system.
 * This server plays the role of Proposer, Acceptor, and Learner in the Paxos algorithm, and it also handles key-value store operations.
 */
public class Server extends UnicastRemoteObject implements ProposerInterface, AcceptorInterface, LearnerInterface, KVStoreInterface {

  private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
  private ConcurrentHashMap<String, String> kvStore = new ConcurrentHashMap<>();
  private AcceptorInterface[] acceptors;
  private LearnerInterface[] learners;
  private final int numServers;
  private final int serverId;
  private static final AtomicInteger sequenceNum = new AtomicInteger(0);
  private int promiseId;
  private final int MAJORITY_COUNT;
  private final String KEY_NOT_FOUND = "Key Not found";
  private static final double FAILURE_RATE = 0.1; // 10% failure rate
  private Random random = new Random();

  /**
   * Constructor to create a Server instance.
   *
   * @param serverId   The unique ID of this server.
   * @param numServers The total number of servers in the system.
   */
  public Server(int serverId, int numServers) throws RemoteException {
    this.numServers = numServers;
    this.serverId = serverId;
    this.promiseId = 0;
    this.MAJORITY_COUNT = Math.floorDiv(numServers, 2) + 1;
  }

  /**
   * Set the acceptors for this server.
   *
   * @param acceptors Array of acceptors.
   */
  public void setAcceptors(AcceptorInterface[] acceptors) {
    this.acceptors = acceptors;
  }

  /**
   * Set the learners for this server.
   *
   * @param learners Array of learners.
   */
  public void setLearners(LearnerInterface[] learners) {
    this.learners = learners;
  }

  @Override
  public synchronized void put(String key, String value) throws RemoteException {
    LOGGER.info("Server" + serverId + " received request: " + Constants.PUT + " " + key + " " + value);
    proposeOperation(new Operation(Constants.PUT, key, value));
  }

  @Override
  public String get(String key) throws RemoteException {
    LOGGER.info("Server" + serverId + " received request: " + Constants.GET + " " + key);
    String value = kvStore.get(key);
    return value != null ? value : KEY_NOT_FOUND;
  }

  @Override
  public synchronized void delete(String key) throws RemoteException {
    LOGGER.info("Server" + serverId + " received request: " + Constants.DELETE + " " + key);
    proposeOperation(new Operation(Constants.DELETE, key));
  }

  @Override
  public int getMapSize() throws RemoteException {
    return kvStore.size();
  }

  /**
   * Propose an operation to be applied.
   *
   * @param operation The operation to be proposed.
   * @throws RemoteException If a remote error occurs.
   */
  private void proposeOperation(Operation operation) throws RemoteException {
    int proposalId = generateProposalId();
    propose(proposalId, operation);
  }

  @Override
  public synchronized int prepare(int proposalId) throws RemoteException {
    // Implement Paxos prepare logic here
    // TODO: Pass failure rate as an argument for easier
    // TODO: https://piazza.com/class/lm865n8sfd7jw/post/193
    if (random.nextDouble() < FAILURE_RATE) {
      throw new RemoteException("Acceptor" + serverId + ": Simulated failure");
    }
    LOGGER.info("Acceptor" + serverId + " received proposal id: " + promiseId);
    if (proposalId > promiseId) {
      promiseId = proposalId;
    }
    LOGGER.info("Acceptor" + serverId + " sending promise id: " + promiseId);
    return promiseId;
  }

  @Override
  public synchronized boolean accept(int proposalId, Object proposalValue) throws RemoteException {
    // Implement Paxos accept logic here
    if (random.nextDouble() < FAILURE_RATE) {
      throw new RemoteException("Acceptor" + serverId + ": Simulated failure");
    }
    LOGGER.info("Acceptor" + serverId + " accepted proposal id: " + proposalId);
    for (int i = 0; i < numServers; i++) {
      learners[i].learn(proposalId, proposalValue);
    }
    return false;
  }

  @Override
  public synchronized void propose(int proposalId, Object proposalValue) throws RemoteException {
    LOGGER.info("Proposer" + serverId + " received request with proposal id: " + proposalId);
    // used to store replies from acceptor
    int[] replies = new int[numServers];
    Arrays.fill(replies, -1);
    int replyCount = 0;
    int acceptedId = 0;
    // Send prepare message to all acceptors
    for (int i = 0; i < numServers; i++) {
      try {
        // store replies from acceptor
        LOGGER.info("Proposer" + serverId + " sending proposal with proposal id: " + proposalId + " to Acceptor" + i);
        replies[i] = acceptors[i].prepare(proposalId);
        acceptedId = Math.max(acceptedId, replies[i]);
        replyCount++;
      } catch (Exception e) {
          LOGGER.info(e.getMessage());
//          throw new RuntimeException(e);
      }
    }

    // if consensus has been reached
    if (replyCount >= MAJORITY_COUNT) {
      LOGGER.warning("Proposer" + serverId + " Consensus has been reached!!!");
      if (acceptedId == proposalId) {
        for (int i = 0; i < numServers; i++) {
          if (replies[i] != -1) {
            try {
              acceptors[i].accept(proposalId, proposalValue);
            } catch (Exception e) {
              LOGGER.info(e.getMessage());
            }
          }
        }
      }
      // If the highest id returned > proposal ID, then
      // generate new id and redo the propose step.
      else if (acceptedId > proposalId) {
        while (acceptedId > proposalId) {
          LOGGER.info("Proposer" + serverId + " Updating the sequence Num!!!");
          proposalId = generateProposalId();
        }
        this.propose(proposalId, proposalValue);
      }
    }
    // If consensus has not been reached.
    else {
      LOGGER.warning("Proposer" + serverId + " Consensus has not been reached!!!");
    }
  }

  @Override
  public synchronized void learn(int proposalId, Object acceptedValue) throws RemoteException {
    // Implement Paxos learn logic here
    applyOperation((Operation) acceptedValue);
    LOGGER.info("Learner" + serverId + " learned proposal id: " + proposalId);
  }

  /**
   * Generates a unique proposal ID.
   *
   * @return A unique proposal ID.
   */
  private int generateProposalId() {
    // Placeholder code to generate a unique proposal ID
    int id = sequenceNum.incrementAndGet();
    LOGGER.info("Generated sequence no: " + id);
    return id;
  }

  /**
   * Apply the given operation to the key-value store.
   *
   * @param operation The operation to apply.
   */
  private void applyOperation(Operation operation) {
    if (operation == null) return;

    switch (operation.type) {
      case Constants.PUT:
        kvStore.put(operation.key, operation.value);
        LOGGER.info("Server" + serverId + " PUT successful!!!");
        break;
      case Constants.DELETE:
        kvStore.remove(operation.key);
        LOGGER.info("Server" + serverId + " DELETE successful!!!");
        break;
      default:
        throw new IllegalArgumentException("Unknown operation type: " + operation.type);
    }
  }

  /**
   * Static class representing an operation on the key-value store.
   */
  private static class Operation {
    String type;
    String key;
    String value;

    Operation(String type, String key, String value) {
      this.type = type;
      this.key = key;
      this.value = value;
    }

    Operation(String type, String key) {
      this(type, key, null);
    }
  }

  // Other methods as needed
}
