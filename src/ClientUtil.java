import java.io.IOException; 
import java.io.*; 
import java.util.*; 

/**
 * Helps with user functions, i.e. logging in and saving users to file
 */
public class ClientUtil {

    private static HashMap<String, String> users = new HashMap<>(); 
    private static File file = new File("users.txt"); 
    private static int userCount = 0; 

    // static initializer to load the users when class is loaded
    static {
        loadUsersFromFile(); 
    }

    /**
     * Load existing user information from file into the hashmap
     */
    private static void loadUsersFromFile() {
        try {
            if (!file.exists()) {
                file.createNewFile();
                return;
            }

            // filtering out the username and password by removing unnecessary elements
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.contains("username:") && line.contains("password:")) {
                    String[] parts = line.split(", password: ");
                    if (parts.length == 2) {
                        String username = parts[0].replace("username: ", "");
                        String encryptedPassword = parts[1];
                        users.put(username, encryptedPassword);
                        userCount++;
                    }
                }
            }
            scanner.close();
            System.out.println("Loaded " + userCount + " users from file");
        } catch (IOException e) {
            System.err.println("Error laoding users from file: " + e.getMessage());
        }
    }

    /**
     * Login a user using a specified username and password 
     * save user to file
     * @param username - username
     * @param password - password 
     * @throws IOException - if somehting goes wrong with authentication
     * @throws InvalidPasswordException - if the password doesn't match the project requirements
     * @throws InvalidUsernameException - if the password doesn't match the project requirements 
     */
    public static void registerUser(String name, String pword) throws Exception, IOException, InvalidPasswordException, InvalidUsernameException, UserExistsException {

        //System.out.println("Attempting to register you . . .");
        if (users.containsKey(name)){
            System.out.println("User already exists, try logging in⚠️");
            throw new UserExistsException("");
        }

        // limit the amount of total users to 10
        if (userCount > 9) {
            System.out.println("No more users, max is 10");
            System.exit(1);
        }

        FileWriter writer = new FileWriter(file, true);

       // System.out.println("Validating username . . .");
            Authenticator.authenticateUsername(name); 

        //System.out.println("Validating password");
            Authenticator.authenticatePassword(pword);

        System.out.println("User created✅");

        userCount++; 
        users.put(name, AESUtil.encrypt(pword));
        writer.write("username: " + name + ", password: " + AESUtil.encrypt(pword) + "\n");
        writer.flush();
        writer.close(); 
    }    

    /**
     * Registers new users using specified usernames and passwords 
     * @param username
     * @param password
     * @throws Exception 
     */
    public static void login(String name, String pword) throws Exception {

        //System.out.println("Logging in . . .");

        // check the username
       if (!users.containsKey(name)) {
            System.out.println("User not in database, need to register first❌");
            throw new Exception(""); 
       }

       String associatedPassword = users.get(name); // password associated with the username 

       // check the password 
       if (!associatedPassword.equals(AESUtil.encrypt(pword))) {
            System.out.println("Incorrect Password❌");
            throw new Exception("");
       }

       System.out.println("Credentials valid, login successful✅");

    }

    /**
     * reads the user file to check for duplicates
     * @param username - username
     * @return - true if there's duplicates, false otherwise 
     */
    public static boolean checkDuplicate(String string) {

        //System.out.println("Scanning for duplicates . . .");

        if (users.containsKey(string)) {
            System.out.println("Username found");
            return true;
        }

        return false; 

    }
}
