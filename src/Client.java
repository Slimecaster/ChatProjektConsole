import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String hostname = "192.168.0.101";
        try (Socket socket = new Socket(hostname, 5000)) {
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            // Thread to read messages from the server and print them to the console
            Thread readMessagesThread = new Thread(() -> {
                String messageFromServer;
                try {
                    while ((messageFromServer = serverReader.readLine()) != null) {
                        System.out.println(messageFromServer);  // Print incoming messages from the server
                    }
                } catch (Exception e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            });

            // Start the reading thread
            readMessagesThread.start();

            // Main thread to handle user input and send messages to the server
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                serverWriter.println(userMessage);  // Send the message to the server
                if (userMessage.equalsIgnoreCase("exit")) {
                    break;  // User can type 'exit' to leave the chat
                }
            }

        } catch (Exception e) {
            System.err.println("Client error:" + e.getMessage());
        }
    }
}