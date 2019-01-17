package ua.in.beroal.java;

public class NonExhaustivePatternMatch extends RuntimeException {
    public NonExhaustivePatternMatch() {
    }

    public NonExhaustivePatternMatch(String message) {
        super(message);
    }

    public NonExhaustivePatternMatch(String message, Throwable cause) {
        super(message, cause);
    }

    public NonExhaustivePatternMatch(Throwable cause) {
        super(cause);
    }

    public NonExhaustivePatternMatch(
            String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
