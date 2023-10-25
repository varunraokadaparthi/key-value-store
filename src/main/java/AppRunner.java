import common.App;
import common.ArgsParser;

public class AppRunner {

  public static void main(String[] args) {

    App app = new ArgsParser().getApp(args);
    app.run();
  }
}
