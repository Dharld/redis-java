import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// This is the protocol parser used to parse the commands
public class ProtocolParser {

    private static final Logger logger = Logger.getLogger(ProtocolParser.class.getName());

    private static final String ECHO_COMMAND = "ECHO";
    private static final String PING_COMMAND = "PING";
    private static final String GET_COMMAND = "GET";
    private static final String SET_COMMAND = "SET";

    // Get the singleton instance of the RedisServer
    private static final RedisServer redisServer = RedisServer.getInstance();

    public static String parse(String command) {
        logger.info("Received command: " + command);

        if (command == null || command.isEmpty()) {
            logger.warning("Command is null or empty");
            return null;
        }

        // Convert the command to uppercase
        String uppercasedCommand = command.toUpperCase(Locale.ROOT);
        logger.info("Uppercased command: " + uppercasedCommand);

        // Split the string according to the delimiter
        String[] parts = command.split("\r\n");
        parts = Arrays.copyOfRange(parts, 3, parts.length);
        logger.info("Command parts: " + Arrays.toString(parts));

        // Check if the command contains the PING command
        if (uppercasedCommand.contains(PING_COMMAND)) {
            return handlePingCommand();
        }
        // Check if the command contains the ECHO command
        else if (uppercasedCommand.contains(ECHO_COMMAND)) {
            return handleEchoCommand(parts);
        }
        else if (uppercasedCommand.contains(GET_COMMAND)) {
            return handleGetCommand(parts);
        }
        else if (uppercasedCommand.contains(SET_COMMAND)) {
            return handleSetCommand(parts);
        }
        // Check if the command contains an unknown command
        else {
            return handleUnknownCommand();
        }
    }

    private static String handlePingCommand() {
        logger.info("Handling PING command");
        return "+PONG\r\n";
    }

    private static String handleSetCommand(String[] parts) {
        logger.info("Handling SET command with parts: " + Arrays.toString(parts));

        // Get the key length and the key
        int keyLength = Integer.parseInt(parts[0].substring(1));
        String key = parts[1];
        logger.info("Key: " + key + ", Key length: " + keyLength);

        // Get the value length and the value
        int valueLength = Integer.parseInt(parts[2].substring(1));
        String value = parts[3];
        logger.info("Value: " + value + ", Value length: " + valueLength);

        // Handle the expiry parameter
        if (parts.length > 7) {
            String parameter = parts[5];
            if (parameter.equalsIgnoreCase("px")) {
                logger.info("Expiry parameter detected: " + parameter);

                // Get the expiry time
                int expiryLength = Integer.parseInt(parts[6].substring(1));
                int expiryTime = Integer.parseInt(parts[7].substring(0, expiryLength));

                logger.info("Parsed expiry length: " + expiryLength);
                logger.info("Parsed expiry time: " + expiryTime + " milliseconds");

                redisServer.setWithTTL(key.substring(0, keyLength), value.substring(0, valueLength), expiryTime, TimeUnit.MILLISECONDS);

                logger.info("Set key with TTL: key=" + key.substring(0, keyLength) + ", value=" + value.substring(0, valueLength) + ", ttl=" + expiryTime + " milliseconds");

                return "+OK\r\n";
            }
        }

        // Set the key and value in the RedisServer
        redisServer.set(key.substring(0, keyLength), value.substring(0, valueLength));

        // Return the response to the user
        return "+OK\r\n";
    }

    private static String handleGetCommand(String[] parts) {
        logger.info("Handling GET command with parts: " + Arrays.toString(parts));

        // Get the key length and the key
        int keyLength = Integer.parseInt(parts[0].substring(1));
        String key = parts[1];
        logger.info("Key: " + key + ", Key length: " + keyLength);

        String value = redisServer.get(key.substring(0, keyLength));

        // Return the response to the user
        if (value == null) {
            logger.info("Value for key " + key + " not found");
            return "$-1\r\n";
        }

        logger.info("GET, this is the received value: " + value);
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }

    private static String handleEchoCommand(String[] parts) {
        logger.info("Handling ECHO command with parts: " + Arrays.toString(parts));

        // Get the second to last part and parse it to integer
        String secondToLastPart = parts[parts.length - 2];
        int numCharacters = Integer.parseInt(secondToLastPart.substring(1));
        logger.info("Number of characters: " + numCharacters);

        // Return the numCharacters first characters of the last part
        String lastPart = parts[parts.length - 1];
        String result = lastPart.substring(0, numCharacters);
        logger.info("Echo result: " + result);

        return "$" + numCharacters + "\r\n" + result + "\r\n";
    }

    private static String handleUnknownCommand() {
        logger.warning("Handling unknown command");
        return "-ERR unknown command";
    }
}