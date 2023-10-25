package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * The {@code LogFormatter} class is a custom formatter for formatting log records
 * into a specific log entry format.
 */
public class LogFormatter extends Formatter {

  /**
   * Formats a log record into a specific log entry format.
   *
   * @param record The log record to format.
   * @return A formatted string representing the log record.
   */
  @Override
  public String format(LogRecord record) {
    return "thread_" + Thread.currentThread().getId() + Constants.LOG_SEPARATOR +
            record.getSourceClassName() + Constants.LOG_SEPARATOR +
            record.getSourceMethodName() + Constants.LOG_SEPARATOR + "\n" +
            record.getLevel() + Constants.LOG_SEPARATOR +
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(record.getMillis())) + Constants.LOG_SEPARATOR +
            record.getMessage() + "\n";
  }
}
