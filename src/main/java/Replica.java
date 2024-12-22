import RESP.RESPEncoder;
import core.ServerConfig;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;



public class Replica {
    private static final Logger logger = Logger.getLogger(Replica.class.getName());
    private Socket socket;
    private int port;
    private ServerConfig config;

    public Replica(Socket socket, int port, ServerConfig config) {
        this.socket = socket;
        this.port = port;
        this.config = config;
    }

    public void start() {
        try(
            // Get channels to communicate with the master
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
        ) {

            // Handshake 1/3
            sendPing(out);
            readMasterResponse(in);

            // Handshake 2/3
            int masterPort = config.getConfig("masterPort") != null ? Integer.parseInt(config.getConfig("masterPort")) : 6379;
            sendReplConf(out, masterPort);
            readMasterResponse(in);

            // Handshake 3/3
            sendPsync(out);
            readMasterResponse(in);

            // Handle replication logic here
        } catch (IOException e) {
            logger.severe("Failed to connect to master: " + e.getMessage());
        }
    }

    private void sendPing(OutputStream outputStream) throws IOException {
        System.out.println("Sending PING to master...");
        // Send a PING request
        String pingRequest = RESPEncoder.encodeArray(new String[] {"PING"});
        outputStream.write(pingRequest.getBytes());
    }

    private void sendReplConf(OutputStream outputStream, int port) throws IOException {
        System.out.println("Sending REPL config to master...");
        String firstRequest = RESPEncoder.encodeArray(new String[] {"REPLCONF", "listening-port", String.valueOf(port)});
        outputStream.write(firstRequest.getBytes());
        String secondRequest = RESPEncoder.encodeArray(new String[] {"REPLCONF", "capa", "psync2"});
        outputStream.write(secondRequest.getBytes());
    }

    private void sendPsync(OutputStream outputStream) throws IOException {
        System.out.println("Sending PSYNC to master...");
        String request = RESPEncoder.encodeArray(new String[] {"PSYNC", "?", "-1"});
        outputStream.write(request.getBytes());
    }

    private void readMasterResponse(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[56];
        int bytesRead = inputStream.read(buffer);

        if (bytesRead > 0) {
            String response = new String(buffer, 0 , bytesRead);
            System.out.println("Received response from master: " + response);
        } else {
            System.out.println("No bytes read from input stream");
        }
    }



}