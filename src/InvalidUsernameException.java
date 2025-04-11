
/**
 * helper class to handle invalid username exceptions
 */
public class InvalidUsernameException extends Exception{

    /**
     * basic constructor 
     * @param message - custom message
     */
    public InvalidUsernameException(String message)  {
        super(message);
    }
}
