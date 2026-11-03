public class InvalidTeamSizeException extends Exception {
    public InvalidTeamSizeException(String message) {
        super(message);
    }

    public InvalidTeamSizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
