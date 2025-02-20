package errorhandler;

public class ErrorHandler {
    public static void reportError(String message) {
        System.err.println("Error: " + message);
        // Optionally, exit or throw an exception
        // System.exit(1);
    }

    public static void reportWarning(String message) {
        System.out.println("Warning: " + message);
    }
}
