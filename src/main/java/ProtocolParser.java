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
    private static final String CONFIG_COMMAND = "CONFIG";
    private static final String KEY_COMMAND = "KEYS";

    // Get the singleton instance of the RedisServer
    private static final Cache redisServer = Cache.getInstance();

    // Get the persistent storage instance
    private static final Config CONFIG = Config.getInstance();

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
        else if (uppercasedCommand.contains(CONFIG_COMMAND)) {
            return handleConfigCommand(parts);
        }

        else if (uppercasedCommand.contains(GET_COMMAND)) {
            return handleGetCommand(parts);
        }

        else if (uppercasedCommand.contains(SET_COMMAND)) {
            return handleSetCommand(parts);
        }

        else if (uppercasedCommand.contains(KEY_COMMAND)) {
            return handleKeyCommand(parts);
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
        // int keyLength = Integer.parseInt(parts[0].substring(1));
        String key = parts[1];
        logger.info("Key: " + key);

        String value = parts[3];
        logger.info("Value: " + value);

        // Handle the expiry parameter
        if (parts.length > 7) {
            // Get the expiry parameter
            String parameter = parts[5];
            if (parameter.equalsIgnoreCase("px")) {
                logger.info("Expiry parameter detected: " + parameter);

                // Get the expiry time
                // int expiryLength = Integer.parseInt(parts[6].substring(1));
                int expiryTime = Integer.parseInt(parts[7]);

                logger.info("Parsed expiry time: " + expiryTime + " milliseconds");

                redisServer.setWithTTL(key, value, expiryTime, TimeUnit.MILLISECONDS);

                logger.info("Set key with TTL: key=" + key + ", value=" + value + ", ttl=" + expiryTime + " milliseconds");

                return "+OK\r\n";
            }
        }

        // Set the key and value in the RedisServer
        redisServer.set(key, value);

        // Return the response to the user
        return "+OK\r\n";
    }

    private static String handleGetCommand(String[] parts) {
        logger.info("Handling GET command with parts: " + Arrays.toString(parts));

        // Get the key length and the key
        // int keyLength = Integer.parseInt(parts[0].substring(1));
        String key = parts[1];
        logger.info("Key: " + key);

        // Get the value associated to the key
        String value = redisServer.get(key);

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

        // Return the numCharacters first characters of the last part
        String lastPart = parts[parts.length - 1];

        logger.info("Echo result: " + lastPart);

        return "$" + lastPart.length() + "\r\n" + lastPart + "\r\n";
    }

    private static String handleConfigCommand(String[] parts) {
        logger.info("Handling CONFIG command with parts: " + Arrays.toString(parts));

        // Get the command and parameter ([$3, GET, $3, dir])
        String command = parts[1];
        String parameter = parts[3];

        // If the command is GET
        if (command.equalsIgnoreCase("GET")) {
            String value = CONFIG.getConfig(parameter);

            // If there's no value associated with the parameter
            if (value == null) {
                return "*-1\r\n";
            }

            // Return the response to the user in RESP format
            return String.format("*2\r\n$%d\r\n%s\r\n$%d\r\n%s\r\n", parameter.length(), parameter, value.length(), value);
        }

        return "-ERR unknown command";
    }

    private static String handleKeyCommand(String[] parts) {
        logger.info("Handling KEYS command with parts: " + Arrays.toString(parts));

        // Get the pattern to match
        String pattern = parts[1];

        // Get all the keys that match the pattern
        String[] keys = Cache.getInstance().keys();

        System.out.println("Showing all keys: ");
        // Testing all the keys
        for (String key : keys) {
            logger.info("Key: " + key);
        }

        // Filter the keys that match the pattern
        String filteredKeys = Arrays.stream(keys)
                .reduce("", (acc, key) -> acc + "$" + key.length() + "\r\n" + key + "\r\n");

        logger.info("Filtered keys: " + filteredKeys);

        // Return the response to the user using this format "*1\r\n$3\r\nfoo\r\n"
        return String.format("*%d\r\n%s", keys.length, filteredKeys);
    }

    private static String handleUnknownCommand() {
        logger.warning("Handling unknown command");
        return "-ERR unknown command";
    }
}