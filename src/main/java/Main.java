import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    int port = 6379;

    try {
      serverSocket = new ServerSocket(port);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      // Continuously accept new connections
      while (true) {
        // Accept the connection only once
        clientSocket = serverSocket.accept();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
          int pingCount = 0;
          while (pingCount < 2) {
            writer.write("+PONG\r\n");
            writer.flush();
            pingCount++;
          }
        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        }
      }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }
}
