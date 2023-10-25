package common;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * The `ConsoleFormatter` class is a custom formatter for formatting log records
 * into a specific format suitable for console output.
 */
public class ConsoleFormatter extends Formatter {

  /**
   * Formats a log record into a custom format for console output.
   *
   * @param record The log record to be formatted.
   * @return A formatted string representing the log record.
   */
  @Override
  public String format(LogRecord record) {
    return "thread_" + Thread.currentThread().getId() + Constants.LOG_SEPARATOR +
            record.getMessage() + "\n";
  }
}
