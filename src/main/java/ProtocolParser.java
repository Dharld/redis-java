import java.util.Locale;

// *2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n
public class ProtocolParser {

    public static String parse(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }

        // Switch the command to uppercase to avoid case sensitivity
        switch(command.toUpperCase(Locale.ROOT)) {
            case "PING":
                return "+PONG\r\n";
            case "ECHO":
                // Split the string according to the delimiter
                String[] parts = command.split("\r\n");

                // Get the second to last part and parse it to integer
                String secondToLastPart = parts[parts.length - 2];
                int numCharacters = Integer.parseInt(secondToLastPart.substring(1));

                // Return the numCharacters first characters of the last part
                String lastPart = parts[parts.length - 1];
                String result = lastPart.substring(0, numCharacters);

                return "$" + numCharacters + "\r\n" + result + "\r\n";

            default:
                return "-ERR unknown command '" + command + "'\r\n";
        }

    }
}
