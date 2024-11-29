import java.io.*;
import java.util.concurrent.TimeUnit;

public class RDBReader {

    public static void readRDBFile(String dir, String filename) throws IOException {
        File file = new File(dir, filename);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            int index = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] fileContent = baos.toByteArray();
            index = 0;
            int fileLength = fileContent.length;

            System.out.println("File length: " + fileLength);

            while (index < fileLength) {
                byte firstByte = fileContent[index];

                if (firstByte == (byte) 0xFB) {
                    // Skip FB
                    System.out.println("Skip FB");
                    index++;

                    // Read the size of the first hashmap (2 bytes)
                    int hashtableSize1 = fileContent[index] & 0xFF;
                    index++;
                    System.out.println("Size of first hashmap: " + hashtableSize1);

                    // Read the size of the second hashmap (2 bytes)
                    int hashtableSize2 = fileContent[index] & 0xFF;
                    index++;
                    System.out.println("Size of second hashmap: " + hashtableSize2);

                    // Read all the different characters
                    while (index < fileLength) {
                        firstByte = fileContent[index];
                        if (firstByte == (byte) 0xFF) {
                            System.out.println("Encountered 0xFF, stopping iteration");
                            break;
                        }
                        else if (firstByte == (byte) 0xFC || firstByte == (byte) 0xFD) {
                            index = readKeyValuePairWithExpiration(fileContent, index);
                        } else {
                            index = readKeyValuePair(fileContent, index);
                        }
                    }

                } else {
                    index++;
                }

            }
        }
    }

    private static int readKeyValuePairWithExpiration(byte[] buffer, int index) {
        long expireTimestamp = -1;
        boolean isMilliseconds = false;

        // If the time is expressed in seconds
        if (buffer[index] == (byte) 0xFC) {
            // Time is gonna be given in milliseconds
            isMilliseconds = true;
            // Skip FC byte
            System.out.println("Skip FC");
            index++;
            // Read the timestamp
            expireTimestamp = readLong(buffer, index);
            System.out.println("Expire timestamp: " + expireTimestamp);
            index += 8;
        } else if (buffer[index] == (byte) 0xFD) {
            // Skip the FD byte
            index++;
            System.out.println("Skip FD");

            // Read the timestamp
            expireTimestamp = readInt(buffer, index);
            System.out.println("Expire timestamp: " + expireTimestamp);
            index += 4;
        }

        // Skip the type of value stored
        byte valueType = buffer[index];
        index++;

        // Read the key length
        int keyLength = readSizeEncodedValue(buffer, index);
        index += getSizeEncodedLength(buffer[index]);

        // Read the key
        String key = new String(buffer, index, keyLength);
        index += keyLength;

        // Read the value length
        int valueLength = readSizeEncodedValue(buffer, index);
        index += getSizeEncodedLength(buffer[index]);

        // Read the value
        String value = new String(buffer, index, valueLength);
        index += valueLength;

        System.out.printf("Key: %s, Value: %s%n", key, value);

        // Store the key-value pair in the cache with expiry
        if (!key.isEmpty() && !value.isEmpty()) {
            Cache.getInstance().setWithTTL(key, value, expireTimestamp, isMilliseconds ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS);
        }

        // For simple delimiter just proceed to the next key-value pair
        if (index < buffer.length && buffer[index] == (byte) 0x00) {
            index++;
        }

        return index;
    }

    private static long readLong(byte[] buffer, int index) {
        return ((buffer[index] & 0xFFL)) |
                ((buffer[index + 1] & 0xFFL) << 8) |
                ((buffer[index + 2] & 0xFFL) << 16) |
                ((buffer[index + 3] & 0xFFL) << 24) |
                ((buffer[index + 4] & 0xFFL) << 32) |
                ((buffer[index + 5] & 0xFFL) << 40) |
                ((buffer[index + 6] & 0xFFL) << 48) |
                ((buffer[index + 7] & 0xFFL) << 56);
    }

    private static int readInt(byte[] buffer, int index) {
        return ((buffer[index] & 0xFF)) |
                ((buffer[index + 1] & 0xFF) << 8) |
                ((buffer[index + 2] & 0xFF) << 16) |
                ((buffer[index + 3] & 0xFF) << 24);
    }


    private static int readKeyValuePair(byte[] buffer, int index) {
        // Read the type of value stored (1 byte)
        byte valueType = buffer[index];
        // Skip the associated byte
        index += 1;
        System.out.println("Skip 00");

        // Read the key length (size-encoded)
        int keyLength = readSizeEncodedValue(buffer, index);
        index += getSizeEncodedLength(buffer[index]);

        // Read the key (keyLength bytes)
        String key = new String(buffer, index, keyLength);
        index += keyLength;

        // Read the value length (size-encoded)
        int valueLength = readSizeEncodedValue(buffer, index);
        index += getSizeEncodedLength(buffer[index]);

        // Read the value (valueLength bytes)
        String value = new String(buffer, index, valueLength);
        index += valueLength;

        // Print the parsed key and value
        System.out.printf("Key: %s, Value: %s%n", key, value);


        if(!key.isEmpty() && !value.isEmpty()) {
            // Store the key-value pair in the cache
            Cache.getInstance().set(key, value);
        }

        return index;
    }

    private static int readSizeEncodedValue(byte[] buffer, int index) {
        byte firstByte = buffer[index];
        int size;

        // 00C0 (1100 0000)
        // firstByte & 0xC0 is used to consider only the first 2 bits of the byte
        if ((firstByte & 0xC0) == 0x00) {
            // 00xxxxxx
            // 3F (0011 1111) is used to mask the lower 6 bits
            size = firstByte & 0x3F;
        } else if ((firstByte & 0xC0) == 0x40) {
            // 01xxxxxx xxxxxxxx
            size = ((firstByte & 0x3F) << 8) | (buffer[index + 1] & 0xFF);
        } else if ((firstByte & 0xC0) == 0x80) {
            // 10xxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx
            size = ((buffer[index + 1] & 0xFF) << 24) | ((buffer[index + 2] & 0xFF) << 16) |
                    ((buffer[index + 3] & 0xFF) << 8) | (buffer[index + 4] & 0xFF);
        } else {
            // 11xxxxxx (string encoding type)
            size = handleStringEncoding(buffer, index);
        }

        return size;
    }

    private static int handleStringEncoding(byte[] buffer, int index) {
        // Handle string encoding types here
        // For this example, we'll just throw an exception
        throw new IllegalArgumentException("String encoding type not supported in this example");
    }

    private static int getSizeEncodedLength(byte firstByte) {
        if ((firstByte & 0xC0) == 0x00) {
            return 1;
        } else if ((firstByte & 0xC0) == 0x40) {
            return 2;
        } else if ((firstByte & 0xC0) == 0x80) {
            return 5;
        } else {
            return 1; // For string encoding type, return 1 for now
        }
    }
}
