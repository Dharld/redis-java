import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Cache {
    // Singleton instance
    private static final Cache instance = new Cache();

    // Store to hold the key-value pairs
    private final Map<String, String> store = new ConcurrentHashMap<>();

    // Scheduler to handle TTL
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger logger = Logger.getLogger(Cache.class.getName());

    private Cache() {
    }

    public static Cache getInstance() {
        return instance;
    }

    public void set(String key, String value) {
//        logger.info("Setting key: " + key + " to value: " + value);

        // Store the key-value pair
        this.store.put(key, value);

//        logger.info("Store after setting key: " + key + " to value: " + value);
//        this.printStore();
    }

    public void setWithTTL(String key, String value, long ttl, TimeUnit unit) {
        // Set first
        set(key, value);

        // Schedule the removal of the key after TTL
        scheduler.schedule(() -> {
//            logger.info("Removing key: " + key + " after TTL");
            store.remove(key);
        }, ttl, unit);
    }

    public String get(String key) {
        return this.store.get(key);
    }

    public String[] keys() {
//        logger.info("Getting all keys");
        return store.keySet().toArray(new String[0]);
    }

    private void printStore() {
        for (Map.Entry<String, String> entry : store.entrySet()) {
            logger.info("Key: " + entry.getKey() + " Value: " + entry.getValue());
        }
    }
}