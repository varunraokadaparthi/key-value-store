package client;

import common.App;
import common.Constants;
import common.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
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
  private static final String REMOTE_OBJECT = "server";

  /**
   * Create the Client App object.
   */
  public ClientApp() {
    this.port = Constants.DEFAULT_RMI_PORT;
  }

  /**
   * Create the Client App object with given server rmi port number.
   * @param port server rmi port number.
   */
  public ClientApp(int port) {
    this.port = port;
  }

  @Override
  public void run() {

    Service service = null;
    try {
      LogManager.getLogManager().readConfiguration(ClientApp.class.getResourceAsStream(CLIENT_LOGGING_PROPERTIES));
      service = (Service) Naming.lookup("rmi://localhost:" + port + "/" + REMOTE_OBJECT);

      prePopulate(service);
    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
      System.exit(-1);
    }

    displayInstructions();
    Scanner in = new Scanner(System.in);
    int status = 0;
    while (true) {
      System.out.println("Enter command: ");
      String input = in.next();
      try {
        switch (input) {
          case "put":
            status = service.put(in.next(), in.next());
            if (status != 0) {
              LOGGER.warning("Put not successful");
            } else {
              LOGGER.info("Put successful");
            }
            break;
          case "post":
            status = service.post(in.next(), in.next());
            if (status != 0) {
              LOGGER.warning("Post not successful");
            } else {
              LOGGER.info("Post successful");
            }
            break;
          case "get":
            LOGGER.info(service.get(in.next()));
            break;
          case "delete":
            status = service.delete(in.next());
            if (status != 0) {
              LOGGER.warning("Delete not successful");
            } else {
              LOGGER.info("Delete successful");
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
  private void prePopulate(Service server) throws RemoteException {
    InputStream inStream = ClientApp.class.getClassLoader().getResourceAsStream(KEY_VALUE_FILE_LOCATION);
    List<String> keyValuePairs = new BufferedReader(new InputStreamReader(inStream))
            .lines()
            .collect(Collectors.toList());
    for (String s : keyValuePairs) {
      if (s.isEmpty()) {
        continue;
      }
      String[] parameters = s.split(" ");
      int status;
      try {
        switch (parameters[0]) {
          case "put":
            if (parameters.length != 3) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
            }
            status = server.put(parameters[1], parameters[2]);
            if (status != 0) {
              LOGGER.warning("Put not successful");
            } else {
              LOGGER.info("Put successful");
            }
            break;
          case "post":
            if (parameters.length != 3) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
            }
            status = server.post(parameters[1], parameters[2]);
            if (status != 0) {
              LOGGER.warning("Post not successful");
            } else {
              LOGGER.info("Post successful");
            }
            break;
          case "get":
            if (parameters.length != 2) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
            }
            LOGGER.info(server.get(parameters[1]));
            break;
          case "delete":
            if (parameters.length != 2) {
              LOGGER.warning(INVALID_PARAMETERS + " : " + s);
            }
            status = server.delete(parameters[1]);
            if (status != 0) {
              LOGGER.warning("Delete not successful");
            } else {
              LOGGER.info("Delete successful");
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
  }

  private static void displayInstructions() {
    String message = "The following commands are available:\n" +
            "1. " + Constants.PUT + " key value\n" +
            "2. " + Constants.POST + " key value\n" +
            "3. " + Constants.GET + " key\n" +
            "4. " + Constants.DELETE + " key\n" +
            "5. help\n" +
            "6. q or quit";
    System.out.println(message);
  }
}