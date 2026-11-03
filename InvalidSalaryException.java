public class InvalidSalaryException extends Exception {
    private final double rejectedValue;

    public InvalidSalaryException(String message) {
        super(message);
        this.rejectedValue = Double.NaN;
    }

    public InvalidSalaryException(String message, Throwable cause) {
        super(message, cause);
        this.rejectedValue = Double.NaN;
    }

    public InvalidSalaryException(String message, double rejectedValue) {
        super(message);
        this.rejectedValue = rejectedValue;
    }

    public InvalidSalaryException(String message, double rejectedValue, Throwable cause) {
        super(message, cause);
        this.rejectedValue = rejectedValue;
    }

    public double getRejectedValue() {
        return rejectedValue;
    }
}
