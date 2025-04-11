
import java.util.regex.*; 
/** Helper class for ClientUtil.java
 * Authenticates user input to match project standards 
 * 
 * Password Rules: 
 * At least 8 characters, 1 digit, 1 uppercase, 1 special character
 * 
 * Username Rules: 
 * alphanumeric, 5-12 characters
 */
public class Authenticator {

    /**
     * Authenticates the password String to verify that it matches the provided requirements
     * @param password
     * @return - true if the password is valid, false otherwise 
     * @throws InvalidPasswordException
     */
    public static boolean authenticatePassword(String password)  throws InvalidPasswordException {

        Pattern passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$");
        Matcher passwordMatcher = passwordPattern.matcher(password);

        if (passwordMatcher.matches()) {
            System.out.println("Password Valid✅: " + passwordMatcher.group());
            return true; 
        }

        System.out.println("Invalid password❌, must fit the following criteria: At least 8 characters, 1 digit, 1 uppercase, 1 special character");
        throw new InvalidPasswordException("");
    }

    /**
     * Authenticates the username String to verify that it matches the provided requirements
     * @param username
     * @return - true if the username is valid, false otherwise
     * @throws InvalidUsernameException
     */
    public static boolean authenticateUsername(String username) throws InvalidUsernameException {

        Pattern passwordPattern = Pattern.compile("[a-zA-Z0-9]{5,12}");
        Matcher passwordMatcher = passwordPattern.matcher(username);

        while (passwordMatcher.matches()) {
            System.out.println("Username Valid✅: " + passwordMatcher.group());
            return true; 
        }

        System.out.println("Invalid username❌, must fit the following criteria: alphanumeric, 5-12 characters");
        throw new InvalidUsernameException("Invalid username: username must be alphanumeric, and contain 5-12 characters");        
    }
}