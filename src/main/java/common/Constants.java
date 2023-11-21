package common;

/**
 * The {@code Constants} class contains various constant values used in the application.
 * These constants define default settings, network-related information, and operation types.
 */
public class Constants {

  /**
   * The default localhost address (IPv4).
   */
  public static final String LOCAL_HOST = "127.0.0.1";

  /**
   * The default port number for network communication.
   */
  public static final int DEFAULT_PORT = 8081;

  /**
   * The default port number for rmi communication.
   */
  public static final int DEFAULT_RMI_PORT = 1099;


  /**
   * Represents the "PUT" request method.
   */
  public static final String PUT = "put";

  /**
   * Represents the "POST" request method.
   */
  public static final String POST = "post";

  /**
   * Represents the "GET" request method.
   */
  public static final String GET = "get";

  /**
   * Represents the "DELETE" request method.
   */
  public static final String DELETE = "delete";

  /**
   * The delimiter used for separating values in data.
   */
  public static final String DELIMITER = ";";

  /**
   * The separator used for logging purposes to distinguish different log entries.
   */
  public static final String LOG_SEPARATOR = "::";

  public static final int STATUS_ABORTED = 1;
  public static final int STATUS_FAILED = -1;
  public static final int STATUS_SUCCESS = 0;
}

