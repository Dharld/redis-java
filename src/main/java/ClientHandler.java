import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import core.ServerConfig;

public class ClientHandler implements Runnable {

    private Master master;
    private Socket clientSocket;
    private ServerConfig config;


    public ClientHandler(Socket clientSocket, Master master, ServerConfig config) {
        this.clientSocket = clientSocket;
        this.master = master;
        this.config = config;
    }

    @Override
    public void run() {
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
                ProtocolParser parser = new ProtocolParser(out, master, config);
                String response = parser.parse(command);

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
