import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Decoder {

    public static String getData(FileInputStream fis) throws IOException {
        // Get the first byte
        int firstByte = fis.read();
        byte firstTwoBits = (byte) getFirstTwoBits(firstByte);

        if(firstTwoBits == 0x00 || firstTwoBits == 0x01 || firstTwoBits == 0x02) {
            // Consider the 6 last bits from the end
            int keyLength = (int) getSizeEncoding(firstTwoBits, firstByte, fis);

            byte[] bytes = new byte[keyLength];
            fis.read(bytes);

            return new String(bytes);
        } else {
            return getStringEncoding((byte) firstByte, fis);
        }

    }

    public static long getSize(FileInputStream fis) throws IOException {
        // Get the first byte
        int firstByte = fis.read();
        byte firstTwoBits = (byte) getFirstTwoBits(firstByte);

        return getSizeEncoding(firstTwoBits, firstByte, fis);
    }

    public static long getSizeEncoding(byte firstTwoBits, int firstByte, FileInputStream fis) throws IOException {
        if(firstTwoBits == 0x00) {
            // Consider the 6 last bits from the end
            return firstByte & 0x3F;
        }
        // The first two bits are 01
        else if(firstTwoBits == 0x01) {
            // Consider the last 14 bits
            int nextByte = fis.read();

            // Take the last 6 bits from the first byte and shift it by 8 to the left
            // Then append the next byte using the bitwise operator
            return (firstByte & 0x3F) << 8 | nextByte;
        }
        // The first two bits are 10
        else {
            // Consider the last 30 bits
            byte[] bytes = readNBytesInBigEndian(fis, 4);

            return ByteBuffer.wrap(bytes).getLong();
        }
    }

    public static String getStringEncoding(byte firstByte, FileInputStream fis) throws IOException {

        int numBytesToRead = -1;

        if (firstByte == (byte) 0xC0) {
            int nextByte = fis.read();
            return Integer.toString(nextByte);
        }
        else if(firstByte == (byte) 0xC1) {
            numBytesToRead = 2;
        }
        else if(firstByte == (byte) 0xC2) {
            numBytesToRead = 4;
        }


        // Read the next numToRead bytes
        byte[] bytes = readNBytesInLittleEndian(fis, numBytesToRead);

        return new String(bytes, 0, bytes.length);
    }

    public static byte[] readNBytesInBigEndian(FileInputStream fis, int n) throws IOException {
        byte[] bytes = new byte[n];
        fis.read(bytes);

        // Convert the bytes to a long
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        return buffer.array();
    }


    public static byte[] readNBytesInLittleEndian(FileInputStream fis, int n) throws IOException {
        byte[] bytes = new byte[n];
        fis.read(bytes);

        // Convert the bytes to a long
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.array();
    }


    public static int getFirstTwoBits(int b) {
        return (b >> 6) & 0x03;
    }
}
