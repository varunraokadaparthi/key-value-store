package server;

import common.Constants;
import common.KVStoreInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
  private static final AtomicInteger sequenceNum = new AtomicInteger(0);
  private final double FAILURE_RATE = 0.25;
  private final boolean failure;
  private final ConcurrentHashMap<String, String> kvStore = new ConcurrentHashMap<>();
  private final int numServers;
  private final int serverId;
  private final int MAJORITY_COUNT;
  private final String KEY_NOT_FOUND = "Key Not found";
  private final Random random = new Random();
  private AcceptorInterface[] acceptors;
  private LearnerInterface[] learners;
  private int promiseId;
  private boolean isPendingAcceptedValue;
  private Object acceptedValue;

  /**
   * Constructor to create a Server instance.
   *
   * @param serverId   The unique ID of this server.
   * @param numServers The total number of servers in the system.
   */
  public Server(int serverId, int numServers, boolean failure) throws RemoteException {
    this.numServers = numServers;
    this.serverId = serverId;
    this.promiseId = 0;
    this.MAJORITY_COUNT = Math.floorDiv(numServers, 2) + 1;
    this.isPendingAcceptedValue = false;
    this.failure = failure;
    if (failure) {
      LOGGER.info("Server" + serverId + " Failure Rate: " + FAILURE_RATE);
    }
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
  public synchronized boolean put(String key, String value) throws RemoteException {
    LOGGER.info("Server" + serverId + " received request: " + Constants.PUT + " " + key + " " + value);
    return proposeOperation(new Operation(Constants.PUT, key, value));
  }

  @Override
  public String get(String key) throws RemoteException {
    LOGGER.info("Server" + serverId + " received request: " + Constants.GET + " " + key);
    String value = kvStore.get(key);
    return value != null ? value : KEY_NOT_FOUND;
  }

  @Override
  public synchronized boolean delete(String key) throws RemoteException {
    LOGGER.info("Server" + serverId + " received request: " + Constants.DELETE + " " + key);
    return proposeOperation(new Operation(Constants.DELETE, key));
  }

  /**
   * Propose an operation to be applied.
   *
   * @param operation The operation to be proposed.
   * @throws RemoteException If a remote error occurs.
   */
  private boolean proposeOperation(Operation operation) throws RemoteException {
    int proposalId = generateProposalId();
    return propose(proposalId, operation);
  }

  @Override
  public synchronized int prepare(int proposalId) throws RemoteException {
    if (failure && random.nextDouble() < FAILURE_RATE) {
      throw new RemoteException("Acceptor" + serverId + ": Simulated failure");
    }
    LOGGER.info("Acceptor" + serverId + " received proposal id: " + proposalId);
    if (isPendingAcceptedValue) {
      LOGGER.info("Acceptor" + serverId + " pending promise id: " + promiseId);
      return promiseId;
    }
    if (proposalId > promiseId) {
      promiseId = proposalId;
    }
    LOGGER.info("Acceptor" + serverId + " sending promise id: " + promiseId);
    return promiseId;
  }

  @Override
  public synchronized boolean accept(int proposalId, Object proposalValue) throws RemoteException {
    // Implement Paxos accept logic here
    if (failure && random.nextDouble() < FAILURE_RATE) {
      throw new RemoteException("Acceptor" + serverId + ": Simulated failure");
    }
    if (proposalId < promiseId) {
      LOGGER.info("Acceptor" + serverId + ": Proposal id " + proposalId + " < " + " Promise id" + promiseId);
      return false;
    }
    LOGGER.info("Acceptor" + serverId + " accepted proposal id: " + proposalId);
    isPendingAcceptedValue = true;
    acceptedValue = proposalValue;
    return true;
  }

  @Override
  public synchronized boolean propose(int proposalId, Object proposalValue) throws RemoteException {
    LOGGER.info("Proposer" + serverId + " received request with proposal id: " + proposalId);
    // used to store replies from acceptor
    int[] replies = new int[numServers];
    Arrays.fill(replies, -1);
    // Send prepare message to all acceptors
    for (int i = 0; i < numServers; i++) {
      try {
        // store replies from acceptor
        LOGGER.info("Proposer" + serverId + " sending proposal with proposal id: " + proposalId + " to Acceptor" + i);
        replies[i] = acceptors[i].prepare(proposalId);
      } catch (Exception e) {
        LOGGER.info(e.getMessage());
      }
    }

    Map<Integer, Integer> frequencyMap = new HashMap<>();
    for (int i = 0; i < numServers; i++) {
      if (replies[i] != -1) {
        frequencyMap.put(replies[i], frequencyMap.getOrDefault(replies[i], 0) + 1);
      }
    }
    int frequentProposalId = -1;
    int frequency = 0;
    for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
      if (entry.getValue() > frequency) {
        frequency = entry.getValue();
        frequentProposalId = entry.getKey();
      }
    }

    int acceptedCount = 0;
    if (frequency >= MAJORITY_COUNT) {
      for (int i = 0; i < numServers; i++) {
        if (replies[i] != -1) {
          try {
            if (acceptors[i].accept(frequentProposalId, proposalValue)) {
              acceptedCount++;
            }
          } catch (Exception e) {
            LOGGER.info(e.getMessage());
          }
        }
      }
      if (acceptedCount >= MAJORITY_COUNT && frequentProposalId == proposalId) {
        LOGGER.warning("Proposer" + serverId + " Consensus has been reached!!!");
        for (int i = 0; i < numServers; i++) {
          try {
            this.learners[i].learn(frequentProposalId, this.acceptedValue);
          } catch (Exception e) {
            LOGGER.info("Proposer" + serverId + " Learner" + i + " failed!!!");
          }
        }
        return true;
      } else {
        // If the frequent id returned > proposal ID, then
        // generate new id and redo the propose step.
        while (frequentProposalId > proposalId) {
          LOGGER.info("Proposer" + serverId + " Updating the sequence Num!!!");
          proposalId = generateProposalId();
        }
        this.propose(proposalId, proposalValue);
      }
    } else {
      // If consensus has not been reached.
      LOGGER.warning("Proposer" + serverId + " Consensus has not been reached!!!");
      return false;
    }
    return false;
  }

  @Override
  public synchronized void learn(int proposalId, Object acceptedValue) throws RemoteException {
    // Implement Paxos learn logic here
    applyOperation((Operation) acceptedValue);
    isPendingAcceptedValue = false;
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
