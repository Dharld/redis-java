package core;

import java.util.HashMap;
import java.util.Map;

public class ServerConfig {

    private final Map<String, String> settings = new HashMap<>();

    public void setConfig(String key, String value) {
        this.settings.put(key, value);
    }

    public String getConfig(String key) {
        return this.settings.get(key);
    }

    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            // Process all the arguments
            if (args[i].startsWith("--")) {
                String argument = args[i].split("--")[1];
                String value = args[i + 1];

                // Get the master port as
                processArgument(argument, value);
            }
        }

        this.settings.putIfAbsent("role", "master");
    }

    private void processArgument(String arg, String value) {
        switch (arg) {
            case "port":
                setConfig("port", value);
                break;
            case "dir":
                setConfig("dir", value);
                break;
            case "dbfilename":
                setConfig("dbfilename", value);
                break;
            case "replicaof":
                String masterHost = value.split(":")[0];
                String masterPort = value.split(":")[1];

                setConfig("role", "slave");
                break;
            default:
                break;
        }

    }

    public boolean isSlave() {
        return this.settings.get("role").equals("slave");
    }

}
