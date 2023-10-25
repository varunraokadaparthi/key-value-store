package common;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConsoleFormatter extends Formatter {

  @Override
  public String format(LogRecord record) {
    return "thread_" + Thread.currentThread().getId() + Constants.LOG_SEPARATOR +
            record.getMessage() + "\n";
  }
}
