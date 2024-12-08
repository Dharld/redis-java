import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    // Get the port number
    int PORT = (args.length > 0 && args.length < 3) ? Integer.parseInt(args[1]) : 6379;

    // Check if the arguments are more than 3
    if (args.length > 3) {
        // Get the dir and dbfilename parameters
        String dir = args[1];
        String dbfilename = args[3];

        // Log the two parameters
        logger.config("dir: " + dir);
        logger.config("dbfilename: " + dbfilename);

        System.out.println("Reading RDB file: " + dbfilename + " in directory: " + dir);

        // Set the parameters in the persistent storage
        Config.getInstance().setConfig("dir", dir);
        Config.getInstance().setConfig("dbfilename", dbfilename);

        // Read the RDB file
        try {
          RDBReader.readRDBFile(dir, dbfilename);
        } catch (IOException e) {
          System.err.println("IOException: " + e.getMessage());
        }
    }

    // Launch the server
    Master master = new Master();
    master.initialize(PORT);

    // Store the port number in the persistent storage for future reference
    Config.getInstance().setPortToServer(PORT, master);

  }


}
