package server;

import common.App;
import common.Constants;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * The {@code ServerApp} class represents a server application that accepts client connections
 * and handles various HTTP-like requests.
 */
public class ServerApp implements App {

  private static final Logger LOGGER = Logger.getLogger(ServerApp.class.getName());
  private static final String SERVER_LOGGING_PROPERTIES = File.separator + "server-logging.properties";
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
      // Total number of servers
      int numServers = 5;

      Server[] servers = new Server[numServers];

      // Create and bind servers
      for (int serverId = 0; serverId < numServers; serverId++) {
        int port = this.port + serverId; // Increment port for each server

        // Create server instance
        servers[serverId] = new Server(serverId, numServers);

        // Create RMI Registry
        Registry registry = LocateRegistry.createRegistry(port);
        // Bind the server to the RMI registry
        registry.rebind(Constants.REMOTE_OBJECT + serverId, servers[serverId]);

        System.out.println("Server " + serverId + " is ready at port " + port);
      }

      // Set acceptors and learners for each server
      for (int serverId = 0; serverId < numServers; serverId++) {
        AcceptorInterface[] acceptors = new AcceptorInterface[numServers];
        LearnerInterface[] learners = new LearnerInterface[numServers];
        for (int i = 0; i < numServers; i++) {
          if (i != serverId) {
            acceptors[i] = servers[i];
            learners[i] = servers[i];
          }
        }
        servers[serverId].setAcceptors(acceptors);
        servers[serverId].setLearners(learners);
      }

    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
    }
  }
}
