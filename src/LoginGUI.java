import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Creates the login and registration interface
 */
public class LoginGUI extends JFrame {

    private JTextField usernameField; 
    private JPasswordField passwordField; 
    private JButton loginButton, registerButton; 
    private JLabel statusLabel; 
    private JPanel mainPanel; 
    private JTextArea terminalArea; 

    /**
     * Actual building of the Login Graphical User Interface 
     */
    public LoginGUI() {

        //Setting up the frame 
        setTitle("SecureChat - Login");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center it on the screen

        //Creating the components 
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 10));

        JLabel titleLabel = new JLabel("SecureChat Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");
        JLabel FYILabel = new JLabel("                 *Make sure to click login");
        JLabel FYILabel_nl = new JLabel("after you register"); 

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);

        //Add componenets to panels
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(FYILabel);
        formPanel.add(FYILabel_nl);

        //Create a terminal area
        terminalArea = new JTextArea(10, 40);
        terminalArea.setEditable(false);
        terminalArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        terminalArea.setBackground(Color.BLACK);
        terminalArea.setForeground(Color.WHITE);
        JScrollPane terminalScrollPane = new JScrollPane(terminalArea);
        terminalScrollPane.setBorder(BorderFactory.createTitledBorder("Terminal Output"));

        // redirect system output to the terminal area
        TextAreaOutputStream.redirectSystemStreams(terminalArea);

        // Add panels to main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        JPanel statusPanel = new JPanel(new BorderLayout()); 
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(terminalScrollPane, BorderLayout.CENTER);                                                
        southPanel.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegistration();
            }
        });

        // Adding a welcome message to the terminal
        System.out.println("SecureChat Terminal");
        System.out.println("Please login or register to continue...");
        
        // show the frame
        setVisible(true);

    }

    /**
     * Handles the login process for each user
     */
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {

            //use ClientUtil to login
            ClientUtil.login(username, password);

            // if successful, open chat window
            statusLabel.setText("Login successful");
            statusLabel.setForeground(Color.GREEN);

            // open the chat window
            SwingUtilities.invokeLater(() -> {
                openChatWindow(username);
            });

        } catch (Exception e) {
            statusLabel.setText("Login failed: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    /**
     * Handles registration for each user
     */
    private void handleRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {

            // validate inpiut fields
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Username and password cannot be empty");
                statusLabel.setForeground(Color.RED);
                return;
            }

            // use ClientUtil to register the user
            ClientUtil.registerUser(username, password); 

            // show success msg if registration was successful
            statusLabel.setText("Registration successful! You can now login.");
            statusLabel.setForeground(Color.GREEN);

        } catch (InvalidUsernameException e) {
            statusLabel.setText("Invalid username: must be alphanumeric, 5-14 characters");
            statusLabel.setForeground(Color.RED);
        } catch (InvalidPasswordException e) {
            statusLabel.setText("Invalid password: must contain 8+ characters, 1 digit, 1 uppercase letter, and 1 special character");
            statusLabel.setForeground(Color.RED);
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    /**
     * Opens the chat window if login was successful
     * @param username
     */
    private void openChatWindow(String username) {

        // close the login window
        dispose();

        //make and show chat window
        SwingUtilities.invokeLater(() -> {
            new ChatGUI(username);
        });
    }

    // main method for testing 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginGUI();
        });
    }
}