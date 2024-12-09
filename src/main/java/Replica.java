import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

enum ReplicaCommand {
    REPLCONF_LISTENING_PORT("*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n%s\r\n"),
    REPLCONF_CAPA("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n"),
    REPLCONF_PSYNC("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n");

    private final String command;

    ReplicaCommand(String command) {
        this.command = command;
    }

    public String getCommand(Object... params) {
        return String.format(command, params);
    }
}

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
            // Send the first REPLCONF command
            String listeningCommand = ReplicaCommand.REPLCONF_LISTENING_PORT.getCommand(6380);
            String capaCommand = ReplicaCommand.REPLCONF_CAPA.getCommand();
            String psyncCommand = ReplicaCommand.REPLCONF_PSYNC.getCommand();

            sendReplConfCommand(out, listeningCommand);
            if (isResponseOk(in)) {
                logger.info("First REPLCONF command successful");
                // Send the second REPLCONF command
                sendReplConfCommand(out, capaCommand);
                if (isResponseOk(in)) {
                    logger.info("Second REPLCONF command successful");
                    // Send the PSYNC command
                    sendReplConfCommand(out, psyncCommand);
                    if (isResponseOk(in)) {
                        logger.info("PSYNC command successful");
                    } else {
                        logger.severe("PSYNC command failed");
                    }
                } else {
                    logger.severe("Second REPLCONF command failed");
                }
            } else {
                logger.severe("First REPLCONF command failed");
            }
        } catch (IOException e) {
            logger.severe("IOException occurred while handling REPLCONF: " + e.getMessage());
            throw e;
        }
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