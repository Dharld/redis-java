import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RedisServer {
    private static final RedisServer instance = new RedisServer();
    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger logger = Logger.getLogger(RedisServer.class.getName());

    private RedisServer() {
    }

    public static RedisServer getInstance() {
        return instance;
    }

    public void set(String key, String value) {
        logger.info("Setting key: " + key + " to value: " + value);
        this.store.put(key, value);
        logger.info("Store after setting key: " + key + " to value: " + value);
        this.printStore();
    }

    public void setWithTTL(String key, String value, long ttl, TimeUnit unit) {
        set(key, value);
        scheduler.schedule(() -> {
            logger.info("Removing key: " + key + " after TTL");
            store.remove(key);
        }, ttl, unit);
    }

    public String get(String key) {
        logger.info("Getting key: " + key);
        return this.store.get(key);
    }

    private void printStore() {
        for (Map.Entry<String, String> entry : store.entrySet()) {
            logger.info("Key: " + entry.getKey() + " Value: " + entry.getValue());
        }
    }
}