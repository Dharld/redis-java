import core.ServerConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Master {
    private static ServerSocket serverSocket = null;

    private ServerConfig config;

    // Parameters for replication
    private String replicationId;
    private int replicationOffset = 0;

    private static String emptyRDB = "524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2";
    static Logger logger = Logger.getLogger(Master.class.getName());

    public Master(ServerConfig config) {
        this.replicationId = generateRandomString();
        this.config = config;
    }

    public void start(int PORT) {
        // Threadpool to handle multiple clients
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        try {
            // Create a new server socket
            serverSocket = new ServerSocket(PORT);
            serverSocket.setReuseAddress(true);

            // keep server running
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                // Run a new thread to handle the client
                threadPool.submit(new ClientHandler(clientSocket, this, config));
            }

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        } finally {
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        }
    }


    private String generateRandomString() {
        return "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    }

    public String getReplicationId() {
        return replicationId;
    }

    public int getReplicationOffset() {
        return replicationOffset;
    }

    public String handleReplicaCommand(String[] parts) {
        logger.info("Handling REPLCONF command with parts: " + Arrays.toString(parts));
        return "+OK\r\n";
    }

    public String handlePsyncCommand(String[] parts, OutputStream out) throws IOException {
        logger.info("Handling PSYNC command with parts: " + Arrays.toString(parts));

        // Get the replication ID and the offset
        String replicationId = parts[1];
        int offset = Integer.parseInt(parts[3]);

        logger.info("Replication ID: " + replicationId);
        logger.info("Offset: " + offset);

        // Set the replication ID and the offset
        if (replicationId.equals("?") && offset == -1) {
            logger.info("Replication ID is equal to ?");

            String fullResyncResponse = String.format("FULLRESYNC %s %d", this.getReplicationId(), this.getReplicationOffset());
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

}
