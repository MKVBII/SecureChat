
/**
 * helper class to handle invalid username exceptions
 */
public class InvalidPasswordException extends Exception {
    /**
     * basic constructor
     * @param message - custom message
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}
