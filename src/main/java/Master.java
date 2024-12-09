import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Master {
    private String role = "master";
    private static ServerSocket serverSocket = null;
    private String replicationId;
    private int replicationOffset = 0;
    private static Master instance = new Master();
    private int[] replicaPorts = {};
    private static String emptyRDB = "524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2";
    static Logger logger = Logger.getLogger(Master.class.getName());

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
                String response = ProtocolParser.parse(command, out);

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

    public static String handleReplicaCommand(String[] parts) {
        logger.info("Handling REPLCONF command with parts: " + Arrays.toString(parts));
        return "+OK\r\n";
    }

    public static String handlePsyncCommand(String[] parts, OutputStream out) throws IOException {
        logger.info("Handling PSYNC command with parts: " + Arrays.toString(parts));

        // Get the replication ID and the offset
        String replicationId = parts[1];
        int offset = Integer.parseInt(parts[3]);

        logger.info("Replication ID: " + replicationId);
        logger.info("Offset: " + offset);

        // Set the replication ID and the offset
        if (replicationId.equals("?") && offset == -1) {
            logger.info("Replication ID is equal to ?");

            String fullResyncResponse = String.format("FULLRESYNC %s %d", Master.getInstance().getReplicationId(), Master.getInstance().getReplicationOffset());
            out.write(String.format("$%d\r\n%s\r\n", fullResyncResponse.length(), fullResyncResponse).getBytes());
            out.flush();

            // Send an empty RDB file
            byte[] rdb = HexFormat.of().parseHex(emptyRDB);

            logger.info("Sending RDB file to the replica");

            String length = "$" + rdb.length + "\r\n";
            out.write(length.getBytes());
            out.write(rdb);
            out.flush();
        }

        // Return the response to the user
        return null;
    }

    public void addToReplicaPorts(int port) {
        int[] newReplicaPorts = new int[replicaPorts.length + 1];
        System.arraycopy(replicaPorts, 0, newReplicaPorts, 0, replicaPorts.length);
        newReplicaPorts[replicaPorts.length] = port;
        replicaPorts = newReplicaPorts;
    }

}
