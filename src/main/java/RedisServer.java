import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisServer {
    private static final RedisServer instance = new RedisServer();
    private final Map<String, String> store = new ConcurrentHashMap<>();

    private RedisServer() {
    }

    public static RedisServer getInstance() {
        return instance;
    }

    public void set(String key, String value) {
        System.out.println("Setting key: " + key + " to value: " + value);
        this.store.put(key, value);
        System.out.println("Store after setting key: " + key + " to value: " + value);
        this.printStore();
    }

    public String get(String key) {
        System.out.println("Getting key: " + key);
        return this.store.get(key);
    }

    private void printStore() {
        for(Map.Entry<String, String> entry : store.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue());
        }
    }
}
