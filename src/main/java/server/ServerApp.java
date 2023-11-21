package server;

import common.App;
import common.Constants;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * The {@code ServerApp} class represents a server application that accepts client connections
 * and handles various HTTP-like requests.
 */
public class ServerApp implements App {

  private static final Logger LOGGER = Logger.getLogger(ServerApp.class.getName());
  private static final String SERVER_LOGGING_PROPERTIES = File.separator + "server-logging.properties";
  private static final String REMOTE_OBJECT = "server";
  private final int port;


  /**
   * Create the Server App object.
   */
  public ServerApp() {
    this.port = Constants.DEFAULT_RMI_PORT;
  }

  /**
   * Create the Server App object with given server rmi port number.
   *
   * @param port server rmi port number.
   */
  public ServerApp(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    try {
      LogManager.getLogManager().readConfiguration(ServerApp.class.getResourceAsStream(SERVER_LOGGING_PROPERTIES));
    } catch (IOException e) {
      System.out.println("Unable to read log properties file!!!");
    }
    // Set the RMI server hostname to localhost
    System.setProperty("java.rmi.server.hostname", Constants.LOCAL_HOST);
    try {
      // Start the coordinator
      TwoPhaseCommitCoordinator coordinator = new TwoPhaseCommitCoordinator();
      Registry coordinatorRegistry = LocateRegistry.createRegistry(port);
      coordinatorRegistry.bind("Coordinator", coordinator);
      LOGGER.info("Coordinator running on port: " + port);

      List<String> hostList = Arrays.asList(Constants.LOCAL_HOST, Constants.LOCAL_HOST, Constants.LOCAL_HOST, Constants.LOCAL_HOST, Constants.LOCAL_HOST);
      List<Integer> portList = new ArrayList<>();

      // Start the participants
      for (int i = 1; i <= 5; i++) {
        Participant participant = new ParticipantImpl(Constants.LOCAL_HOST, port, port + i);
        Registry participantRegistry = LocateRegistry.createRegistry(port + i);
        participantRegistry.bind("Server", participant);
        LOGGER.info("Participant " + i + " running on port: " + (port + i));
        portList.add(port + i);
      }
      // Set participants in coordinator
      coordinator.setParticipants(hostList, portList);

    } catch (RemoteException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
    } catch (AlreadyBoundException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
    }
  }
}
