import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

enum Command {
    ECHO("ECHO"),
    PING("PING"),
    GET("GET"),
    SET("SET"),
    CONFIG("CONFIG"),
    KEYS("KEYS"),
    INFO("INFO"),
    REPLCONF("REPLCONF"),
    PSYNC("PSYNC");

    private final String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}

// This is the protocol parser used to parse the commands
public class ProtocolParser {

    private static final Logger logger = Logger.getLogger(ProtocolParser.class.getName());

    // Get the singleton instance of the RedisServer
    private static final Database redisServer = Database.getInstance();

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
        if (uppercasedCommand.contains(Command.PING.getCommand())) {
            return handlePingCommand();
        }
        // Check if the command contains the ECHO command
        else if (uppercasedCommand.contains(Command.ECHO.getCommand())) {
            return handleEchoCommand(parts);
        }
        else if (uppercasedCommand.contains(Command.CONFIG.getCommand())) {
            return handleConfigCommand(parts);
        }

        else if (uppercasedCommand.contains(Command.GET.getCommand())) {
            return handleGetCommand(parts);
        }

        else if (uppercasedCommand.contains(Command.SET.getCommand())) {
            return handleSetCommand(parts);
        }

        else if (uppercasedCommand.contains(Command.KEYS.getCommand())) {
            return handleKeyCommand(parts);
        }

        else if (uppercasedCommand.contains(Command.INFO.getCommand())) {
            return handleInfoCommand(parts);
        }

        else if(uppercasedCommand.contains(Command.REPLCONF.getCommand())) {
            return handleReplicaCommand(parts);
        }

        else if(uppercasedCommand.contains(Command.PSYNC.getCommand())) {
            return handlePsyncCommand(parts);
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

                // Get the current time
                long currentTime = System.currentTimeMillis();
                long expiryTimestamp = currentTime + expiryTime;

                redisServer.setWithExpiry(key, value, expiryTimestamp);

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
        logger.info("Handling KEYS command with * parts: " + Arrays.toString(parts));
        logger.info("Test");

        // Get the pattern to match
        String pattern = parts[1];

        logger.info("Getting keys");
        // Get all the keys that match the pattern
        String[] keys = redisServer.keys();

        logger.info("Got keys");
        logger.info("Keys: " + Arrays.toString(keys));

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

    private static String handleInfoCommand(String[] parts) {
        logger.info("Handling INFO command with parts: " + Arrays.toString(parts));

        // Check if we have the --replicaof flag
        if (Config.getInstance().getConfig("replicaof") != null) {
            // We are dealing with a replica
            return "$10\r\nrole:slave\r\n";
        }

        String replicationId = Master.getInstance().getReplicationId();
        int replicationOffset = Master.getInstance().getReplicationOffset();

        String info = String.format("role:master\r\nmaster_replid:%s\r\nmaster_repl_offset:%d", replicationId, replicationOffset);
        logger.info(info);

        return "$" + info.length() + "\r\n" + info + "\r\n";
    }

    private static String handleReplicaCommand(String[] parts) {
        logger.info("Handling REPLCONF command with parts: " + Arrays.toString(parts));
        return "+OK\r\n";
    }

    private static String handlePsyncCommand(String[] parts) {
        logger.info("Handling PSYNC command with parts: " + Arrays.toString(parts));

        // Get the replication ID and the offset
        String replicationId = parts[1];
        int offset = Integer.parseInt(parts[3]);

        logger.info("Replication ID: " + replicationId);
        logger.info("Offset: " + offset);

       // Set the replication ID and the offset
        if (replicationId.equals("?")) {
            // Return
            logger.info("Replication ID is not equal to ?");
            String fullResyncResponse = String.format("+FULLRESYNC %s %d", Master.getInstance().getReplicationId(), Master.getInstance().getReplicationOffset());
            return "$" + fullResyncResponse.length() + "\r\n" + fullResyncResponse + "\r\n";
        }

        // Return the response to the user
        return "+OK\r\n";
    }
    private static String handleUnknownCommand() {
        logger.warning("Handling unknown command");
        return "-ERR unknown command";
    }
}