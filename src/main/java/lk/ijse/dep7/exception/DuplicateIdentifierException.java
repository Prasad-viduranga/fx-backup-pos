package lk.ijse.dep7.exception;

public class DuplicateIdentifierException extends Exception{

    public DuplicateIdentifierException() {
    }

    public DuplicateIdentifierException(String message) {

    }

    public DuplicateIdentifierException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateIdentifierException(Throwable cause) {
        super(cause);
    }
}
