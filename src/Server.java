import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static List<ClientHandler> clientList = new CopyOnWriteArrayList<>(); // Thread-safe list

    public static void main(String[] args) {
        int threadPoolSize = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server started on port 5000");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, clientList);
                threadPool.submit(clientHandler);  // Handle the client in a separate thread
                clientList.add(clientHandler);     // Add client to the list
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
