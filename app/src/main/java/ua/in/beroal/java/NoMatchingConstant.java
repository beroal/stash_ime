package ua.in.beroal.java;

public class NoMatchingConstant extends RuntimeException {
    public NoMatchingConstant() {
    }

    public NoMatchingConstant(String message) {
        super(message);
    }

    public NoMatchingConstant(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMatchingConstant(Throwable cause) {
        super(cause);
    }

    public NoMatchingConstant(
            String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
