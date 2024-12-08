import java.io.*;

import java.util.logging.Logger;

public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getName());

  private static void processArgument(String arg, String value) {
    switch (arg) {
        case "port":
            int port = Integer.parseInt(value);
            logger.config("PORT: " + port);
            Config.getInstance().setConfig("port", Integer.toString(port));
            break;
        case "dir":
            logger.config("dir: " + value);
            Config.getInstance().setConfig("dir", value);
            break;
        case "dbfilename":
            logger.config("dbfilename: " + value);
            Config.getInstance().setConfig("dbfilename", value);
            break;
        case "replicaof":
            logger.config("replicaof: " + value);
            Config.getInstance().setConfig("replicaof", value);
            break;
        default:
            logger.warning("Unknown argument: " + arg);
            break;
    }
  }

  public static void main(String[] args) {
    // Get the port number
    for (int i = 0; i < args.length; i++) {
        logger.config("args[" + i + "]: " + args[i]);
        // Process all the arguments
        if (args[i].startsWith("--")) {
            String argument = args[i].split("--")[1];
            String value = args[i + 1];
            processArgument(argument, value);
        }
    }

    Config config = Config.getInstance();

    // Check if the arguments are more than 3
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

    initialize();

  }

  private static void initialize() {
      Config config = Config.getInstance();

      // Create either a replica or a master based on the configuration
      if (config.getConfig("replicaof") != null) {
          String replicaOf = config.getConfig("replicaof");
          logger.info("Starting as replica of: " + replicaOf);
      }

      int port = 6379;
      if (config.getConfig("port") != null) {
          port = Integer.parseInt(config.getConfig("port"));
      }
      logger.info("Starting as master on port: " + port);
      Master.getInstance().initialize(port);

      // Store the master server to the config
      config.setPortToServer(port, Master.getInstance());




  }
}
