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
        process(clientSocket)
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

  public void process(Socket clientSocket) {
    // Get the reader and the writer
    try (BufferedWriter writer =
                 new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
         BufferedReader reader =
                 new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    ) {

      // String to keep track of the content
      String content;

      // Read the content and write +PONG to the client
      while ((content = reader.readLine()) != null) {
        System.out.println("Received: " + content);

        if ("ping".equalsIgnoreCase(content)) {
          writer.write("+PONG\r\n");
          writer.flush();
        } else if ("eof".equalsIgnoreCase(content)) {
          // Close the connection
          System.out.println("Closing the connection");
        }

      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }

  }
}
