package checkmate.insea.user.exception;

public class AuthException extends RuntimeException{
    public AuthException(String message){
        super(message);
    }

    public AuthException(String message, Throwable cause){
        super(message, cause);
    }
}
