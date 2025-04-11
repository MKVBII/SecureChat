import javax.swing.*; 
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat; 
import java.util.Date; 

/**
 * Builds the Chat Graphical User Interface
 */
public class ChatGUI extends JFrame { 

    // attributes and stuff
    private JTextArea chatArea; 
    private JTextField messageField; 
    private JButton sendButton, logoutButton, clearButton, exportButton; 
    private JLabel statusLabel;

    private Socket socket; 
    private PrintWriter output;
    private BufferedReader input;
    private String username; 
    private boolean connected = false; 

    private String serverAddress = "127.0.0.1";
    private int port = 5100; 

    // file for chat logging 
    private File logFile = new File("chat_log.txt");
    private FileWriter logWriter; 

    /**
     * Actual setup
     */
    @SuppressWarnings("unused")
    public ChatGUI(String username) {

        // Setup main frame
        setTitle("SecureChat - " + username);
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components 
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Chat area
        chatArea = new JTextArea(); 
        chatArea.setEditable(false);
        chatArea.setFocusable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea); 
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 10));
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        logoutButton = new JButton("Logout");
        clearButton = new JButton("Clear Chat");
        exportButton = new JButton("Export Log");
        buttonPanel.add(logoutButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);

        // Status Label 
        statusLabel = new JLabel("Disconnected", SwingConstants.RIGHT);
        statusLabel.setForeground(Color.RED);

        // add components to bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        // add panels to the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add main panel to the frame
        add(mainPanel);

        // Add action listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        logoutButton.addActionListener(e -> logout());
        clearButton.addActionListener(e -> clearChat());
        exportButton.addActionListener(e -> exportChatLog());

        // window closing event - ensure a clean disconnect
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });

        // show the frame 
        setVisible(true);

        // connect to server 
        connectToServer();

        // set focus to message field
        messageField.requestFocus();
    }

    /**
     * Method to connect chat users to the server
     */
    private void connectToServer() {
        // start a background thread for connection
        new Thread(() -> {
            try {
                // update status 
                updateStatus("Connect to server...", Color.ORANGE);

                // Connect to server
                socket = new Socket(serverAddress, port);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // open log file
                try {
                    logWriter = new FileWriter(logFile, true);
                } catch (IOException e) {
                    addToChatArea("Error opening log file: " + e.getMessage());
                }
                
                // update status
                updateStatus("connected to server at " + serverAddress + ":" + port, Color.GREEN);
                connected = true; 

                // welcome message
                addToChatArea("*Connected to server. Welcome to SecureChat! \nTUTORIAL:\n"+
                              "*Your first message should be your username (not necessary but helps with clean processing)\n"+ 
                              "*Logout button: logs you out and returns you to the login page\n"+
                              "*lear chat: clears the chat\n"+
                              "*Export Log: allows you to save a recording of the chat to your system\n"+
                              "*Send: sends your message to the common chat space");
                

                //start messafge reader thread
                new Thread(this::readMessages).start();

            } catch (IOException e) {
                updateStatus("Failed to connect to server: " + e.getMessage(), Color.RED);
                addToChatArea("Failed to connect to server: " + e.getMessage());
            }

        }).start(); 
    }

    /**
     * Reads, decrypts, and displays messages to the common chat area
     */
    private void readMessages() {
        try {
            String message; 
            while (connected && (message = input.readLine()) != null) {

                // decrypt the message
                String decryptedMessage = AESUtil.decrypt(message);

                // add to chat area
                String display = "Server: " + decryptedMessage; 
                addToChatArea(display);
                
                //Log message
                logMessage("server", message, decryptedMessage);

                // check for exit message
                if (decryptedMessage.equalsIgnoreCase("exit")) {
                    addToChatArea("Server has ended the chat.");
                    disconnect();
                    break; 
                }
            }
        } catch (Exception e) {
            if (connected) {
                addToChatArea("Error reading from server: " + e.getMessage());
                disconnect();
            }
        }
    }

    /**
     * Provides the function allowing users to send messages to the common chat space
     */
    private void sendMessage() {

        if (!connected) {
            addToChatArea("Not connected to server");
            return;
        }

        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return; 
        }

        try {

            // encrypt message
            String encryptedMessage = AESUtil.encrypt(message);

            // send to server
            output.println(encryptedMessage);

            // add to chat area
            addToChatArea("You: " + message); 

            // log message
            logMessage(username, encryptedMessage, message);

            // check for exit message
            if (message.equalsIgnoreCase("exit")) {
                addToChatArea("You have exited the chat.");
                disconnect();
            }

            // clear message field
            messageField.setText("");

        } catch (Exception e) {
            addToChatArea("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Adds the messages to the common chat area 
     * @param message
     */
    private void addToChatArea(String message) {

        //get timestamp 
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date());

        // add to chat area
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[" + time + ']' + message + "\n");
            //auto scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    /**
     * Gives user feedback as to whats going on with the server connection
     * @param message
     * @param color
     */
    private void updateStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
        });
    }

    /**
     * Logs the message into the 'chat_log.txt' file
     * @param sender
     * @param encryptedMessage
     * @param plainMessage
     */
    private void logMessage(String sender, String encryptedMessage, String plainMessage) {
        try {
            if (logWriter != null) {
                logWriter.write("|" + sender + "; enc: " + encryptedMessage + " |");
                logWriter.write("|" + sender + "; reply: " + plainMessage + " |\n");
                logWriter.flush();
            }
        } catch (IOException e) {
            addToChatArea("Error logging message: " + e.getMessage());
        }
    }

    /**
     * Clears the common chat (does not affect the 'chat_log.txt')
     */
    private void clearChat() {
        chatArea.setText("");
    }

    /**
     * allows you to save the file to your system
     */
    private void exportChatLog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Chat Log");
        fileChooser.setSelectedFile(new File("chat_export.txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter fw = new FileWriter(fileToSave)) {
                fw.write(chatArea.getText());
                addToChatArea("Chat log exported to " + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                addToChatArea("Error exporting log: " + e.getMessage());
            }
        }
    }

    /**
     * allows the user to log out of the system 
     */
    private void logout() {
        // ask for confirmation
        int choice  = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // send exit message
            if (connected) {
                try {
                    String exitMessage = AESUtil.encrypt("exit");
                    output.println(exitMessage);
                } catch (Exception e) {
                    // just log the error
                    System.err.println("Error sending exit message: " + e.getMessage());
                }
            }

            // disconnect and close the window
            disconnect();
            dispose();

            // show the login window again
            SwingUtilities.invokeLater(() -> {
                new LoginGUI();
            });
        }
    }

    /**
     * disconnects the user from the common chat and closes their chat window
     */
    private void disconnect() {
        connected = false; 

        try {
            if (logWriter != null) {
                logWriter.close();
            }

            if (input != null) {
                input.close();
            }

            if (output != null) {
                output.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            updateStatus("Disconnected", Color.RED); 
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

}
