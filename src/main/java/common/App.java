package common;

/**
 * The `App` interface defines a common contract for application classes that
 * have a `run` method, which is typically used to start the application's execution.
 */
public interface App {

  /**
   * This method is called to start the execution of the application.
   * Implementing classes should provide the necessary logic for the application to run.
   */
  void run();
}
