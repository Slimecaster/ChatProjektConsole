import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private String username;
    private PrintWriter writer;
    private List<ClientHandler> clientList;

    public ClientHandler(Socket socket, List<ClientHandler> clientList) {
        this.socket = socket;
        this.clientList = clientList;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("Enter your username: ");
            username = reader.readLine();
            writer.println("Username entered, welcome: " + username);
            broadcastMessage("Server: " + username + " has joined the chat.");

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    break;  // User can type 'exit' to disconnect
                }

                // Check if the message is a whisper command
                if (message.startsWith("/w ")) {
                    handleWhisperCommand(message);
                } else {
                    broadcastMessage(username + ": " + message);
                }
            }

        } catch (IOException e) {
            System.err.println("Error in ClientHandler: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clientList) {
            if (client != this) {  // Don't send the message to the sender
                client.writer.println(message);
            }
        }
    }

    // Handle the whisper command "/w recipientUsername message"
    private void handleWhisperCommand(String message) {
        String[] splitMessage = message.split(" ", 3);  // "/w recipientUsername message"
        if (splitMessage.length < 3) {
            writer.println("Server: Invalid whisper command. Use /w <username> <message>");
            return;
        }

        String recipientUsername = splitMessage[1];
        String whisperMessage = splitMessage[2];

        ClientHandler recipient = findClientByUsername(recipientUsername);
        if (recipient != null) {
            recipient.writer.println("Whisper from " + username + ": " + whisperMessage);  // Send private message to the recipient
            writer.println("You whispered to " + recipientUsername + ": " + whisperMessage);  // Confirm to the sender
        } else {
            writer.println("Server: User " + recipientUsername + " not found.");
        }
    }

    // Find a client by their username
    private ClientHandler findClientByUsername(String username) {
        for (ClientHandler client : clientList) {
            if (client.username.equalsIgnoreCase(username)) {
                return client;
            }
        }
        return null;
    }

    private void closeConnection() {
        try {
            clientList.remove(this);  // Remove client from the list when disconnected
            broadcastMessage("Server: " + username + " has left the chat.");
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection for " + username);
        }
    }
}
