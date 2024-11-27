import java.util.Arrays;
import java.util.Locale;

// This is the protocol parser used to parse the commands
public class ProtocolParser {

    private static final String EOF_COMMAND = "EOF";
    private static final String ECHO_COMMAND = "ECHO";
    private static final String PING_COMMAND = "PING";
    private static final String GET_COMMAND = "GET";
    private static final String SET_COMMAND = "SET";

    // Get the singleton instance of the RedisServer
    private static final RedisServer redisServer = RedisServer.getInstance();

    public static String parse(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }

        // Convert the command to uppercase
        String uppercasedCommand = command.toUpperCase(Locale.ROOT);

        // Split the string according to the delimiter
        String[] parts = command.split("\r\n");
        parts = Arrays.copyOfRange(parts, 3, parts.length);

        System.out.println("Those are the required parts: " + Arrays.toString(parts));

        // Check if the command contains the PING command
        if (uppercasedCommand.contains(PING_COMMAND)) {
            return handlePingCommand();
        }
        // Check if the command contains the ECHO command
        else if (uppercasedCommand.contains(ECHO_COMMAND)) {
            return handleEchoCommand(parts);
        }

        else if(uppercasedCommand.contains(GET_COMMAND)){
            return handleGetCommand(parts);
        }
        else if(uppercasedCommand.contains(SET_COMMAND)){
            return handleSetCommand(parts);
        }
        // Check if the command contains an unknown command
        else {
            return handleUnknownCommand();
        }
    }

    private static String handlePingCommand() {
        return "+PONG\r\n";
    }

    private static String handleSetCommand(String[] parts) {
        // Get the key length and the key
        int keyLength = Integer.parseInt(parts[0].substring(1));
        String key = parts[1];

        // Get the value length and the value
        int valueLength = Integer.parseInt(parts[2].substring(1));
        String value = parts[3];


        // Set the key and value in the RedisServer
        redisServer.set(key.substring(0, keyLength), value.substring(0, valueLength));

        // Return the response to the user
        return "+OK\r\n";
    }

    private static String handleGetCommand(String[] parts) {
        // Get the key length and the key

        for(String part: parts){
            System.out.println("PART: " + part);
        }

        int keyLength = Integer.parseInt(parts[0].substring(1));
        String key = parts[1];

//        System.out.println("KEY: " + key);
//        System.out.println("GETTING THE VALUE ASSOCIATED TO: " + key.substring(keyLength));

        String value = redisServer.get(key.substring(0, keyLength));

        // Return the response to the user
        if (value == null) {
            return "$-1\r\n";
        }

        System.out.println("GET, this is the received value: " + value);
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }


    private static String handleEchoCommand(String[] parts) {


        // Get the second to last part and parse it to integer
        String secondToLastPart = parts[parts.length - 2];
        int numCharacters = Integer.parseInt(secondToLastPart.substring(1));

        // Return the numCharacters first characters of the last part
        String lastPart = parts[parts.length - 1];
        String result = lastPart.substring(0, numCharacters);

        return "$" + numCharacters + "\r\n" + result + "\r\n";
    }


    private static String handleUnknownCommand() {
        return "-ERR unknown command";
    }
}