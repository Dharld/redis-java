import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

public class Replica {
    private static final Logger logger = Logger.getLogger(Replica.class.getName());
    private String masterAddress;
    private int masterPort;

    public Replica(String masterAddress, int masterPort) {
        this.masterAddress = masterAddress;
        this.masterPort = masterPort;
    }


    public void initialize(String masterAddress, int masterPort) {
        this.masterAddress = masterAddress;
        this.masterPort = masterPort;
        try(Socket socket = new Socket(masterAddress, masterPort)) {
            // Get channels to communicate with the master
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Send a *1\r\n$4\r\nPING\r\n command to the master in resp format
            out.write("*1\r\n$4\r\nPING\r\n".getBytes());
            out.flush();

            // Read the response from the master
            String response = in.readAllBytes().toString();

            // Log the response
            logger.info("Response from master: " + response);

            // Connect to the master
            logger.info("Connected to master at " + masterAddress + ":" + masterPort);
            // Handle replication logic here
        } catch (IOException e) {
            logger.severe("Failed to connect to master: " + e.getMessage());
        }
    }
}