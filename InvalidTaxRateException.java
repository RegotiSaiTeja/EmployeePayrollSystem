public class InvalidTaxRateException extends RuntimeException {
    public InvalidTaxRateException(String message) {
        super(message);
    }

    public InvalidTaxRateException(String message, Throwable cause) {
        super(message, cause);
    }
}
