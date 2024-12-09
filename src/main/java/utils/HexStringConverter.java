package utils;

public class HexStringConverter {
    public static void main(String[] args) {
        String hexString = "524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2";

        // Convert hex string to byte array
        byte[] byteArray = hexStringToByteArray(hexString);

        // Print the byte array
        System.out.println("Array of bytes:");
        for (byte b : byteArray) {
            System.out.printf("%02x ", b);
        }
    }

    // Utility method to convert hex string to byte array
    public static byte[] hexStringToByteArray(String hex) {
        int length = hex.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    | Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
