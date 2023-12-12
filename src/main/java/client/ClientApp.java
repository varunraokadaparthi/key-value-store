package client;

import common.App;
import common.Constants;
import common.KVStoreInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The {@code ClientApp} class represents a client application that communicates with a server
 * using various commands for key-value operations. It provides a command-line interface for
 * interacting with the server.
 */
public class ClientApp implements App {

  private static final Logger LOGGER = Logger.getLogger(ClientApp.class.getName());
  private static final String KEY_VALUE_FILE_LOCATION = "key_value.txt";
  private static final String INVALID_PARAMETERS = "Invalid parameters length!!!";
  private static final String CLIENT_LOGGING_PROPERTIES = File.separator + "client-logging.properties";
  private final int port;
  private static final int REMOTE_SERVERS_COUNT = 5;

  /**
   * Create the Client App object.
   */
  public ClientApp() {
    this.port = Constants.DEFAULT_RMI_PORT;
  }

  /**
   * Create the Client App object with given server rmi port number.
   *
   * @param port server rmi port number.
   */
  public ClientApp(int port) {
    this.port = port;
  }

  @Override
  public void run() {

    KVStoreInterface server;
    Random random = new Random();
    int randomPortOffset;
    try {
      LogManager.getLogManager().readConfiguration(ClientApp.class.getResourceAsStream(CLIENT_LOGGING_PROPERTIES));
      randomPortOffset = random.nextInt(REMOTE_SERVERS_COUNT) + 1;
      server = (KVStoreInterface) Naming.lookup("rmi://localhost:" + (port + randomPortOffset) + "/" + Constants.REMOTE_OBJECT + randomPortOffset);
      LOGGER.info("Connected to Server: " + randomPortOffset + " Port: " + (port + randomPortOffset));
      prePopulate(server);
    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      System.exit(-1);
    }

    displayInstructions();
    Scanner in = new Scanner(System.in);
    while (true) {
      System.out.println("Enter command: ");
      String input = in.next();
      try {
        randomPortOffset = random.nextInt(REMOTE_SERVERS_COUNT) + 1;
        server = (KVStoreInterface) Naming.lookup("rmi://localhost:" + (port + randomPortOffset) + "/" + Constants.REMOTE_OBJECT + randomPortOffset);
        LOGGER.info("Connected to Server: " + randomPortOffset + " Port: " + (port + randomPortOffset));
        switch (input) {
          case Constants.PUT:
            server.put(in.next(), in.next());
            LOGGER.info(Constants.PUT + " successful");
            break;
          case Constants.GET:
            LOGGER.info(server.get(in.next()));
            break;
          case Constants.DELETE:
            server.delete(in.next());
            LOGGER.info(Constants.DELETE + " successful");
            break;
          case "help":
            displayInstructions();
            break;
          case "q":
          case "quit":
            break;
          default:
            LOGGER.warning("Invalid Command!!!");
            break;
        }
        if ("q".equals(input) || "quit".equals(input)) {
          LOGGER.info("Closing Application!!!");
          System.exit(0);
          break;
        }
      } catch (Exception e) {
        LOGGER.severe(e.getMessage());
      }
    }
  }

  /**
   * Reads requests from a file and executes them.
   *
   * @throws IOException If an I/O error occurs during the operation.
   */
  private void prePopulate(KVStoreInterface server) throws RemoteException {
    InputStream inStream = ClientApp.class.getClassLoader().getResourceAsStream(KEY_VALUE_FILE_LOCATION);
    List<String> keyValuePairs = new BufferedReader(new InputStreamReader(inStream))
            .lines()
            .collect(Collectors.toList());
    LOGGER.info("Pre Populating...");
    for (String s : keyValuePairs) {
      if (s.isEmpty()) {
        continue;
      }
      String[] parameters = s.split(" ");
      try {
        switch (parameters[0]) {
          case Constants.PUT:
            if (parameters.length != 3) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
              break;
            }
            server.put(parameters[1], parameters[2]);
            LOGGER.info(Constants.PUT + " successful");
            break;
          case Constants.GET:
            if (parameters.length != 2) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
              break;
            }
            LOGGER.info(server.get(parameters[1]));
            break;
          case Constants.DELETE:
            if (parameters.length != 2) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
              break;
            }
            server.delete(parameters[1]);
            LOGGER.info(Constants.DELETE + " successful");
            break;
          default:
            LOGGER.warning("Invalid Command!!! " + s);
            break;
        }
      } catch (RemoteException e) {
        System.out.println("Exception: " + e.getMessage());
        LOGGER.severe(e.getMessage());
      }
    }
    LOGGER.info("Done with Pre Population!!!");
  }

  private static void displayInstructions() {
    String message = "The following commands are available:\n" +
            "1. " + Constants.PUT + " key value\n" +
            "2. " + Constants.GET + " key\n" +
            "3. " + Constants.DELETE + " key\n" +
            "4. help\n" +
            "5. q or quit";
    System.out.println(message);
  }
}
