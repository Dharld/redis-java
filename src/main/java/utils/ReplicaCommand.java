package utils;

public enum ReplicaCommand {
    REPLCONF_LISTENING_PORT("*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n%s\r\n"),
    REPLCONF_CAPA("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n"),
    REPLCONF_PSYNC("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n");

    private final String command;

    ReplicaCommand(String command) {
        this.command = command;
    }

    public String getCommand(Object... params) {
        return String.format(command, params);
    }
}