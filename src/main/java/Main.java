import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {

  private static final int PORT = 6379;
  private static final Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    System.out.println("Server started on port: " + PORT);

    // Check if the number of arguments is correct
    if (args.length > 3) {
      // Get the dir and dbfilename parameters
      String dir = args[1];
      String dbfilename = args[3];

      // Log the two parameters
      logger.config("dir: " + dir);
      logger.config("dbfilename: " + dbfilename);

      System.out.println("Reading RDB file: " + dbfilename + " in directory: " + dir);

      // Set the parameters in the persistent storage
      PersistentStorage.getInstance().setConfig("dir", dir);
      PersistentStorage.getInstance().setConfig("dbfilename", dbfilename);

      // Read the RDB file
        try {
            RDBReader.readRDBFile(dir, dbfilename);
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }


    ServerSocket serverSocket = null;

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

  private static void process(Socket clientSocket) {
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
}
