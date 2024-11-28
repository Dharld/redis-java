import java.io.*;

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
                if (fileContent[index] == (byte) 0xFB) {
                    // Skip FB
                    index += 1;

                    // Read the size of the first hashmap (2 bytes)
                    int hashtableSize1 = ((fileContent[index] & 0xFF) << 8) | (fileContent[index + 1] & 0xFF);
                    // Skip the associated byte
                    index += 1;
                    System.out.println("Size of first hashmap: " + hashtableSize1);

                    // Read the size of the second hashmap (2 bytes)
                    int hashtableSize2 = ((fileContent[index] & 0xFF) << 8) | (fileContent[index + 1] & 0xFF);
                    // Skip the associated byte
                    index += 1;
                    System.out.println("Size of second hashmap: " + hashtableSize2);

                    // Read the type of value stored (1 byte)
                    byte valueType = fileContent[index];
                    // Skip the associated byte
                    index += 1;
                    System.out.println("Type of value stored: " + valueType);

                    // Read the key-value pairs
                    System.out.println("Reading key-value pairs");
                    while (index < fileLength && fileContent[index] != (byte) 0xFF) {
                        // Read the key length (size-encoded)
                        index = readKeyValuePair(fileContent, index);
                    }
                }
                index++;
            }
        }
    }

    private static int readKeyValuePair(byte[] buffer, int index) {
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


        // Store the key-value pair in the cache
        Cache.getInstance().set(key, value);

        return index;
    }

    private static int readSizeEncodedValue(byte[] buffer, int index) {
        int firstByte = buffer[index] & 0xFF;
        int size;

        // 00C0(1100 0000)
        // firstByte & 0xC0 is used to consider only the first 2 bits of the byte
        if ((firstByte & 0xC0) == 0x00) {
            // 00xxxxxx
            // 3F(0011 1111) is used to
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
