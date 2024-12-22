import core.ServerConfig;
import java.io.*;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerConfig config = new ServerConfig();

        // Parse the arguments
        config.parseArgs(args);

        // Read the RDB file
        if (config.getConfig("dir") != null || config.getConfig("dbfilename") != null) {
            String dir = config.getConfig("dir");
            String dbfilename = config.getConfig("dbfilename");

            // Read the RDB file
            try {
              RDBReader.readRDBFile(dir, dbfilename);
            } catch (IOException e) {
              System.err.println("IOException: " + e.getMessage());
            }
        }

        if(config.isSlave()) {
            // Start the replica server
            startReplicaServer(config);
        } else {
            // Start the master server
            startMasterServer(config);
        }
    }

    private static void startReplicaServer(ServerConfig config) throws IOException {
        String masterHost = config.getConfig("masterHost");

        // Get the master and the port
        int masterPort = Integer.parseInt(config.getConfig("masterPort"));
        int port = Integer.parseInt(config.getConfig("port"));

        Socket serverSocket = new Socket(masterHost, masterPort);
        Replica replica = new Replica(serverSocket, port, config);
        replica.start();
    }

    private static void startMasterServer(ServerConfig config) throws IOException{
        // Start the master server
        int port = Integer.parseInt(config.getConfig("port"));

        Master masterServer = new Master(config);
        masterServer.start(port);
    }
}
