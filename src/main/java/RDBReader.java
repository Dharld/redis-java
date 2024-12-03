import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RDBReader {
    public static Map<String, String> metadata = new HashMap<>();
    public static Database database = Database.getInstance();
    public static Logger logger = Logger.getLogger(RDBReader.class.getName());


    public static void readRDBFile(String dir, String dbfilename) throws IOException {
        // Create the database
        // createFile(testData);

        File file = new File(dir + "/" + dbfilename);

        try(FileInputStream fis = new FileInputStream(file)) {
            // Read the header
            String header = readHeader(fis);
            logger.info("This is the header: " + header);

            // Test the current byte
            // int currentByte = fis.read();
            // logger.info(String.format("Current byte: 0x%02X", currentByte));

            // Read the metadata
            readMetadata(fis);

            // Test the metadata
            metadata.forEach((key, value) -> {
                logger.info("Key: " + key + ", Value: " + value);
            });

            // Read the index
            readIndex(fis);

            // Read the data
            getData(fis);

        } catch(IOException e) {
            // Throw meaningful exception
            e.printStackTrace();
            // throw new RuntimeException("Error reading the RDB file");
        }

    }

    private static void getData(FileInputStream fis) throws IOException {

        // Read the next value FB
        byte currentByte = (byte) fis.read();
        logger.info(String.format("Start Byte: 0x%02X", currentByte));

        // Read until we reach FF
        while(currentByte != (byte) 0xFF) {
            // Start reading data if we reach FB

            if(currentByte == (byte) 0xFB) {
                readData(fis);
            }

            currentByte = (byte) fis.read();
        }
    }

    private static void readIndex(FileInputStream fis) throws IOException {
        // Read the index
        int dbIndex = fis.read();
        logger.info("Database index: " + dbIndex);

        database.setId(dbIndex);
    }

    private static void readData(FileInputStream fis) throws IOException {
        // Read the two hashmaps size
        long h1Size = Decoder.getSize(fis);
        long h2Size = Decoder.getSize(fis);

        logger.info("Hashmap 1 size: " + h1Size);
        logger.info("Hashmap 2 size: " + h2Size);


        boolean isFirstHashmap = true;

        // Continuously read the key-value pairs until we reach the byte FF
        int currentByte = fis.read();
        printByteString((byte) currentByte);


        long timestamp = 0;
        boolean isMillisecond = false;

        while (currentByte != 0xFF) {

            // Time is in milliseconds, and value is stored in the first hashmap
            if (currentByte == 0xFC || currentByte == 0xFD) {
                isFirstHashmap = false;
                isMillisecond = currentByte == 0xFC;
                timestamp = currentByte == 0xFD ? readInt(fis) : readLong(fis);
                logger.info("Timestamp: " + timestamp);
            }

            // Read the key value pair
            if (isFirstHashmap) {
                readDatabaseData(fis);
            } else {
                readDatabaseDataWithExpiry(fis, timestamp, isMillisecond);
            }

            // Read the next byte
            currentByte = fis.read();

        }

    }

    private static void printByteString(byte b) {
        logger.info(String.format("Current byte: 0x%02X", b));
    }

    private static void readDatabaseData(FileInputStream fis) throws IOException {

        // Read the key length
        String key = Decoder.getData(fis);
        logger.info("Key: " + key);

        String value = Decoder.getData(fis);
        logger.info("Value: " + value);

        // Store the key-value pair
        database.set(key, value);
        logger.info("Key-value pair: stored");
    }

    private static void readDatabaseDataWithExpiry(FileInputStream fis, long timestamp, boolean isMillisecond) throws IOException {
        int type = fis.read();
        logger.info("Type: " + type);

        // Read the key length
        String key = Decoder.getData(fis);
        logger.info("Key: " + key);

        String value = Decoder.getData(fis);
        logger.info("Value: " + value);


        TimeUnit unit = isMillisecond ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS;

        // Store the key-value pair
        database.setWithExpiry(key, value, timestamp, unit);
        logger.info("Key-value pair with expiry: stored");
    }

    private static int readInt(FileInputStream fis) throws IOException {
        // Read 4 bytes
        byte[] bytes = new byte[4];
        fis.read(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
    }

    private static long readLong(FileInputStream fis) throws IOException {
        // Read 8 bytes
        byte[] bytes = new byte[8];
        fis.read(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    public static void createFile(byte[] data) {
        // Create a file with the given data
        File file = new File("data.dat");

        try(FileOutputStream fos = new FileOutputStream(file)) {
            for (byte b : data) {
                fos.write(b);
            }
        } catch(IOException e) {
            throw new RuntimeException();
        }

    }

    public static String readHeader(FileInputStream fis) throws IOException {
        // The header is composed of: Magic String + Version
        // Magic String: 6 bytes
        // Version: 3 bytes
        // String: REDIS001

        byte[] header = new byte[9];
        fis.read(header);

        // Convert the header to a String
        return new String(header);
    }

    private static void readMetadata(FileInputStream fis) throws IOException {
        // FA                             // Indicates the start of a metadata subsection.
        // 09 72 65 64 69 73 2D 76 65 72  // The name of the metadata attribute (string encoded): "redis-ver".
        // 06 36 2E 30 2E 31 36 FA

        // Skip the FA byte
        int currentByte = fis.read();


        // Read the key-value pairs until we reach the byte FE
        while (currentByte !=  0xFE) {
            if(currentByte ==  0xFA) {
                // Read the metadata line
                readMetadataLine(fis);
            }
            // Read the next byte
            currentByte = fis.read();
//            logger.info("Current byte: " + currentByte);
        }
    }

    private static void readMetadataLine(FileInputStream fis) throws IOException {
        // Read the key length
        String key = Decoder.getData(fis);
        logger.info("Key: " + key);

        String value = Decoder.getData(fis);
        logger.info("Value: " + value);

        // Store the key-value pair
        metadata.put(key, value);
        logger.info("Key-value pair: stored");
    }

}
