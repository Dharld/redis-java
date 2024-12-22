package RESP;

public class RESPEncoder {

    public static String encodeArray(String[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(array.length).append("\r\n");
        for (String element : array) {
            sb.append(encodeString(element));
        }
        return sb.toString();
    }

    public static String encodeString(String str) {
        return "$" + str.length() + "\r\n" + str + "\r\n";
    }
}