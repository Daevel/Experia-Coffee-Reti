package logger;

public class Log {

    // Livelli di log
    public enum Level {
        INFO, WARNING, ERROR, DEBUG, SUCCESS
    }

    private static void log(Level level, String message) {
        System.out.println("[" + level + "] " + message);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void success(String message) {
        log(Level.SUCCESS, message);
    }

    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }
}
