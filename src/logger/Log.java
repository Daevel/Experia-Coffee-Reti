package logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    // Livelli di log
    public enum Level {
        INFO, WARNING, ERROR, DEBUG, SUCCESS
    }

    // Definisci il formato per la data e l'ora
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static void log(Level level, String message) {
        // Ottieni l'ora corrente e formattala
        String timestamp = LocalDateTime.now().format(formatter);
        // Stampa il messaggio con il timestamp
        System.out.println("[" + timestamp + "] [" + level + "] " + message);
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