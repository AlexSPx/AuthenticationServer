package bg.sofia.uni.fmi.mjt.authserver.logger.crash;

import java.io.UncheckedIOException;

/**
 * An interface for logging exceptions.
 */
public interface ExceptionLogger {
    /**
     * Logs an exception and returns the path of the log file.
     *
     * @param e The exception to be logged.
     * @return The path of the log file.
     * @throws UncheckedIOException if there is an issue with logging the exception.
     */
    String log(Exception e);
}
