import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  private static final int PORT = 6379;
  private static final String PING_COMMAND = "ping";
  private static final String EOF_COMMAND = "eof";

  public static void main(String[] args) {
    System.out.println("Server started on port: " + PORT);

    ServerSocket serverSocket = null;
    ExecutorService threadPool = Executors.newFixedThreadPool(10);

    try {
      serverSocket = new ServerSocket(PORT);
      serverSocket.setReuseAddress(true);

      while (true) {
        final Socket clientSocket = serverSocket.accept();
        threadPool.submit(() -> process(clientSocket));
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
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
         BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

      String content;
      while ((content = reader.readLine()) != null) {
        System.out.println("Received: " + content);

        if (PING_COMMAND.equalsIgnoreCase(content)) {
          writer.write("+PONG\r\n");
          writer.flush();
        } else if (EOF_COMMAND.equalsIgnoreCase(content)) {
          System.out.println("Closing the connection.");
          break;
        }
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
