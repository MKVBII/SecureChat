/**
 * Helper class to help assist with attempts to register already existin users
 */
public class UserExistsException extends Exception {

    /**
     * basic constructor 
     * @param message - custom message
     */
    public UserExistsException(String message) {
        super(message); 
    }
    
}
