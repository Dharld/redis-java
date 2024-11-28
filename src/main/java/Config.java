import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Config {
    // Singleton instance
    private static final Config instance = new Config();

    // Store to hold the key-value pairs
    private final Map<String, String> config = new ConcurrentHashMap<>();

    private Logger logger = Logger.getLogger(Config.class.getName());

    private Config() {
    }

    public static Config getInstance() {
        return instance;
    }

    public void setConfig(String key, String value) {
//        logger.info("Setting key: " + key + " to value: " + value);

        // Store the key-value pair
        this.config.put(key, value);

//        logger.info("Store after setting key: " + key + " to value: " + value);
//        this.printConfig();
    }


    public String getConfig(String key) {
//        logger.info("Getting key: " + key);
        return this.config.get(key);
    }

    private void printConfig() {
        for (Map.Entry<String, String> entry : config.entrySet()) {
            logger.info("Key: " + entry.getKey() + " Value: " + entry.getValue());
        }
    }
}
