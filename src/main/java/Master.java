import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master {
    private String role = "master";
    private static ServerSocket serverSocket = null;
    private String replicationId;
    private int replicationOffset = 0;
    private static Master instance = new Master();

    private Master() {
        this.replicationId = generateRandomString();
    }

    public void initialize(int PORT) {
        // Threadpool to handle multiple clients
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        try {
            // Create a new server socket
            serverSocket = new ServerSocket(PORT);
            serverSocket.setReuseAddress(true);

            // keep server running
            while (true) {
                final Socket clientSocket = serverSocket.accept();

                // Span a new thread to handle the new client
                Runnable task = () -> process(clientSocket);
                threadPool.submit(task);
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } finally {
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        }
    }

    private void process(Socket clientSocket) {
        // Open the input and output streams
        try (OutputStream out = clientSocket.getOutputStream();
             InputStream in = clientSocket.getInputStream();)
        {

            // Get the input stream
            byte[] bytes = new byte[1024];
            int bytesRead;

            // Read just bytesRead element
            while ((bytesRead = in.read(bytes)) != -1) {
                // Convert the bytes to a string
                String command = new String(bytes, 0, bytesRead);

                // Parse the command
                String response = ProtocolParser.parse(command);
                out.write(response.getBytes());
                out.flush();
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Failed to close socket: " + e.getMessage());
            }
        }
    }

    private String generateRandomString() {
        return "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    }

    public static Master getInstance() {
        return instance;
    }

    public String getReplicationId() {
        return replicationId;
    }

    public int getReplicationOffset() {
        return replicationOffset;
    }

    public String getRole() {
        return role;
    }



}
