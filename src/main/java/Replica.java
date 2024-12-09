import utils.ReplicaCommand;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;



public class Replica {
    private static final Logger logger = Logger.getLogger(Replica.class.getName());
    private String masterAddress;
    private int masterPort;

    public Replica(String masterAddress, int masterPort) {
        this.masterAddress = masterAddress;
        this.masterPort = masterPort;
    }

    private void sendReplConfCommand(OutputStream out, String command) throws IOException {
        out.write(command.getBytes());
        out.flush();
    }

    private boolean isResponseOk(BufferedReader in) throws IOException {
        String response = in.readLine();
        return response.equals("+OK");
    }

    private void handleReplConf(OutputStream out, BufferedReader in) throws IOException {
        try {
            if (!executeReplConfCommand(out, in, ReplicaCommand.REPLCONF_LISTENING_PORT.getCommand(6380))) {
                logger.severe("First REPLCONF command failed");
                return;
            }
            logger.info("First REPLCONF command successful");

            if (!executeReplConfCommand(out, in, ReplicaCommand.REPLCONF_CAPA.getCommand())) {
                logger.severe("Second REPLCONF command failed");
                return;
            }
            logger.info("Second REPLCONF command successful");

            if (!executeReplConfCommand(out, in, ReplicaCommand.REPLCONF_PSYNC.getCommand())) {
                logger.severe("PSYNC command failed");
                return;
            }
            logger.info("PSYNC command successful");
        } catch (IOException e) {
            logger.severe("IOException occurred while handling REPLCONF: " + e.getMessage());
            throw e;
        }
    }

    private boolean executeReplConfCommand(OutputStream out, BufferedReader in, String command) throws IOException {
        sendReplConfCommand(out, command);
        return isResponseOk(in);
    }

    public void connectToMaster() {
        try(Socket socket = new Socket(masterAddress, masterPort)) {
            // Get channels to communicate with the master
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send a *1\r\n$4\r\nPING\r\n command to the master in resp format
            out.write("*1\r\n$4\r\nPING\r\n".getBytes());
            out.flush();

            // Read the response from the master
            String response = in.readLine();

            // Send first REPLCONF
            if(response.equals("+PONG")) {
                handleReplConf(out, in);
            }

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