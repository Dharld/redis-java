import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  private static final int PORT = 6379;

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
    try (OutputStream out = clientSocket.getOutputStream();
         InputStream in = clientSocket.getInputStream();)
    {

      // Get the input stream
      byte[] bytes = new byte[1024];
      int bytesRead;

      // Read just bytesRead element
      while ((bytesRead = in.read(bytes)) != -1) {
          String command = new String(bytes, 0, bytesRead);
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
