package innowice.java.hackathon.exception;

public class ServiceException extends Exception{

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}