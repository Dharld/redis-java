import java.util.Locale;

// This is the protocol parser used to parse the commands
public class ProtocolParser {

    private static final String EOF_COMMAND = "EOF";
    private static final String ECHO_COMMAND = "ECHO";
    private static final String PING_COMMAND = "PING";

    public static String parse(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }

        // Convert the command to uppercase
        String uppercasedCommand = command.toUpperCase(Locale.ROOT);

        // If we have a PING command, return +PONG
        if (uppercasedCommand.contains(PING_COMMAND)) {

            return "+PONG\r\n";

        }
        // If we have an ECHO command, return the number of characters specified
        else if (uppercasedCommand.contains(ECHO_COMMAND)) {

            // Split the string according to the delimiter
            String[] parts = command.split("\r\n");

            // Get the second to last part and parse it to integer
            String secondToLastPart = parts[parts.length - 2];
            int numCharacters = Integer.parseInt(secondToLastPart.substring(1));

            // Return the numCharacters first characters of the last part
            String lastPart = parts[parts.length - 1];
            String result = lastPart.substring(0, numCharacters);

            return "$" + numCharacters + "\r\n" + result + "\r\n";

        }
        // If we have an EOF command, return +OK
        else if (uppercasedCommand.contains(EOF_COMMAND)) {

            return "+OK\r\n";

        }

        // If we have an unknown command, return -ERR unknown command
        else {

            return "-ERR unknown command '" + command + "'\r\n";

        }
    }
}
