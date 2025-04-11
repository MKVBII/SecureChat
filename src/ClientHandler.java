import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Implements threaded multi-client support 
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username; 
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static FileWriter logWriter;

    /**
     * Static method to initialize the log file for every time the class is called (helps the log stay up to date)
     */
    static {
        try {
            // initialize shared log file writer
            File logFile = new File("chat_log.txt");
            logWriter = new FileWriter(logFile, true);
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
        }
    }

    /**
     * handles the addition of a new client 
     * @param socket
     */
    public ClientHandler(Socket socket) {
        this.socket = socket; 
        try {
            // initialize streams
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);

            //read first message (should contain username)
            String firstMessage = input.readLine();
            if (firstMessage != null) {
                // try to decrypt the username
                try {
                    this.username = AESUtil.decrypt(firstMessage);
                } catch (Exception e) {
                    this.username = "Anonymous-" + socket.getInetAddress().getHostAddress();
                }
            } else {
                this.username = "Anonymous-" + socket.getInetAddress().getHostAddress();
            }

            synchronized (clients) {
                clients.add(this);
            }

            System.out.println("New client connected: " + username + " from " + socket.getInetAddress());
            broadcastServerMessage(username + " has joined the chat");
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error decrypting username: " + e.getMessage());
        }
    }


    /**
     * handles client operations until they logout/disconnect
     */
    @Override
    public void run() {
        String encryptedMessage; 

        try {
            // process messages until client disconnects
            while ((encryptedMessage = input.readLine()) != null) {
                String message; 

                try {
                    //decrypt the message
                    message = AESUtil.decrypt(encryptedMessage);

                    // Log message to consol and file
                    System.out.println(username + "(enc): " + encryptedMessage);
                    System.out.println(username + ": " + message);
                    logMessage(username, encryptedMessage, message);

                    // check for exit message
                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println(username + " has exited the chat.");
                        break;
                    }

                    // broadcast messages to all other clients 
                    broadcastMessage(message);
                } catch (Exception e) {
                    System.err.println("Error decrypting message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            // clean up resources 
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }

            // remove from list of clients
            synchronized (clients) {
                clients.remove(this);
            }

            // notify eveyone about client disconnect
            broadcastServerMessage(username + " has left the chat.");
        }
    }

    /**
     * Send message to this client
     * @param message
     */
    public void sendMessage(String message) {
        try {
            //encrypt message
            String encryptedMessage = AESUtil.encrypt(message);

            //send encrypted message
            output.println(encryptedMessage);

            // Log message
            logMessage("server", encryptedMessage, message);
        } catch (Exception e) {
            System.err.println("Error sending message from " + username + ": " + e.getMessage());
        }
    }

    /**
     * Broadcast message to all clients except this one
     * @param message
     */
    private void broadcastMessage(String message) {
        String formattedMessage = username + ": " + message; 
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    client.sendMessage(formattedMessage);
                }
            }
        }
    }

    /**
     * Broadcast server message to all clients
     * @param message
     */
    private static void broadcastServerMessage(String message) {
        String formattedMessage = "Server: " + message;
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(formattedMessage);
            }
        }
    }

    /**
     * Log messages to file
     * @param sender
     * @param encryptedMessage
     * @param plainMessage
     */
    private void logMessage(String sender, String encryptedMessage, String plainMessage) {
        try {
            synchronized (logWriter) {
                logWriter.write("|" + sender + "; enc: " + encryptedMessage + " |");
                logWriter.write("|" + sender + "; reply: " + plainMessage + " |\n");
                logWriter.flush();
            }
        } catch (IOException e) {
            System.err.println("Error loggin message: " + e.getMessage());
        }
    }



}
