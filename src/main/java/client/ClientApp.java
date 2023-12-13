package client;

import common.App;
import common.Constants;
import common.KVStoreInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
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

  private KVStoreInterface getServer() throws MalformedURLException, NotBoundException, RemoteException {
    KVStoreInterface server;
    Random random = new Random();
    int randomPortOffset;
    randomPortOffset = random.nextInt(REMOTE_SERVERS_COUNT) + 1;
    server = (KVStoreInterface) Naming.lookup("rmi://localhost:" + (port + randomPortOffset) + "/" + Constants.REMOTE_OBJECT + randomPortOffset);
    LOGGER.info("Connected to Server: " + randomPortOffset + " Port: " + (port + randomPortOffset));
    return server;
  }

  @Override
  public void run() {

    try {
      LogManager.getLogManager().readConfiguration(ClientApp.class.getResourceAsStream(CLIENT_LOGGING_PROPERTIES));
      prePopulate(getServer());
    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      System.exit(-1);
    }

    displayInstructions();
    Scanner in = new Scanner(System.in);
    String[] parameters = {};
    boolean retry = false;
    int retryCount = 0;
    while (true) {
      if (!retry) {
        System.out.println("Enter command: ");
        String input = in.nextLine();
        parameters = input.split(" ");
        parameters[0] = parameters[0].toUpperCase();
      } else {
        LOGGER.info("Retry: Connecting to another server!!!");
      }
      try {
        switch (parameters[0]) {
          case Constants.PUT:
            if (parameters.length != 3) {
              LOGGER.warning("Invalid Command!!!");
              break;
            }
            if (getServer().put(parameters[1], parameters[2])) {
              LOGGER.info(Constants.PUT + " successful");
            } else {
              LOGGER.info(Constants.PUT + " failed");
            }
            break;
          case Constants.GET:
            if (parameters.length != 2) {
              LOGGER.warning("Invalid Command!!!");
              break;
            }
            LOGGER.info(getServer().get(parameters[1]));
            break;
          case Constants.DELETE:
            if (parameters.length != 2) {
              LOGGER.warning("Invalid Command!!!");
              break;
            }
            if (getServer().delete(parameters[1])) {
              LOGGER.info(Constants.DELETE + " successful");
            } else {
              LOGGER.info(Constants.DELETE + " failed");
            }
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
        retry = false;
        if ("q".equals(parameters[0]) || "quit".equals(parameters[0])) {
          LOGGER.info("Closing Application!!!");
          System.exit(0);
          break;
        }
      } catch (Exception e) {
        retryCount++;
        if (retryCount > REMOTE_SERVERS_COUNT) {
          LOGGER.info("Max retries!!!");
          retry = false;
          retryCount = 0;
        } else {
          retry = true;
          LOGGER.severe(e.getMessage());
        }
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
      parameters[0] = parameters[0].toUpperCase();
      try {
        switch (parameters[0]) {
          case Constants.PUT:
            if (parameters.length != 3) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
              break;
            }
            if (server.put(parameters[1], parameters[2])) {
              LOGGER.info(Constants.PUT + " successful");
            } else {
              LOGGER.info(Constants.PUT + " failed");
            }
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
            if (server.delete(parameters[1])) {
              LOGGER.info(Constants.DELETE + " successful");
            } else {
              LOGGER.info(Constants.DELETE + " failed");
            }
            break;
          default:
            LOGGER.warning("Invalid Command!!! " + s);
            break;
        }
      } catch (RemoteException e) {
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
