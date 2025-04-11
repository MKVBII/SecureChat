import java.io.*;
import java.net.*;

/**
 * Class to setup and start the chat server
 */
public class ChatServer {

    private static final int PORT = 5100; // Port number to listen on

    public static void main(String[] args) throws Exception {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for clients...");

            while (true) {
                //Wait for client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getInetAddress());

                // Create a new handler for this client 
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                // start client handler in a new thread
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting serve: " + e.getMessage());
        }
    } 
}
