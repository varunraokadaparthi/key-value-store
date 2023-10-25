package server;

import common.App;
import common.Constants;
import common.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
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


  public ServerApp() {
    this.port = Constants.DEFAULT_RMI_PORT;
  }

  public ServerApp(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    try {
      LogManager.getLogManager().readConfiguration(ServerApp.class.getResourceAsStream(SERVER_LOGGING_PROPERTIES));
    } catch (IOException e) {
      System.out.println("Unable to read log properties file!!!");
      ;
    }

    System.setProperty("java.rmi.server.hostname", Constants.LOCAL_HOST);
    try {
      Server server = new Server();
      Service serverStub = (Service) UnicastRemoteObject.exportObject(server, 0);
      LocateRegistry.createRegistry(port);
      LOGGER.info("Local Registry to the server has been created!!!");
      Naming.bind("server", serverStub);
      LOGGER.info("Server remote object has been registered in the registry");
    } catch (RemoteException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
    } catch (AlreadyBoundException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
    } catch (MalformedURLException e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getStackTrace().toString());
    }
  }
}
