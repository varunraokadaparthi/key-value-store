package common;

import client.ClientApp;
import server.ServerApp;

public class ArgsParser {

  public App getApp(String[] args) {
    if (args.length == 2 || args.length > 3) {
      System.out.println("Invalid arguments!!!");
      System.exit(-1);
    }
    App app = null;
    if (args.length == 0) {
      app = new ServerApp();
    }
    if (args.length == 1) {
      if ("-server".equals(args[0])) {
        app = new ServerApp();
      } else if ("-client".equals(args[0])) {
        app = new ClientApp();
      } else {
        System.out.println("Invalid arguments!!!");
        System.exit(-1);
      }
    }
    if (args.length == 3) {
      if ("-server".equals(args[0])) {
        if ("-port".equals(args[1])) {
          app = new ServerApp(Integer.parseInt(args[2]));
        } else {
          System.out.println("Invalid arguments!!!");
          System.exit(-1);
        }
      } else if ("-client".equals(args[0])) {
        if ("-port".equals(args[1])) {
          app = new ClientApp(Integer.parseInt(args[2]));
        } else {
          System.out.println("Invalid arguments!!!");
          System.exit(-1);
        }
      } else {
        System.out.println("Invalid Parameter!!!");
        System.exit(-1);
      }
    }
    return app;
  }
}
