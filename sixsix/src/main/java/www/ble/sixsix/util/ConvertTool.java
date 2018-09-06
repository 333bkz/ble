package www.ble.sixsix.util;

public final class ConvertTool {

    /**
     * 将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 将一个byte转换为16进制的string格式
     */
    public static String byteToHexString(byte b) {
        StringBuilder builder = new StringBuilder("");
        int temp = b & 0xFF;
        String tempString = Integer.toHexString(temp);
        if (tempString.length() < 2) {
            builder.append(0);
        }
        builder.append(tempString);
        return builder.toString();
    }

    /**
     * int转byte[]
     *
     * @param asc true:顺序 false:倒序
     */
    public static byte[] getBytes(int s, boolean asc) {
        byte[] buf = new byte[4];
        if (asc)
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        else
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        return buf;
    }

    /**
     * Byte[2]数组转int
     * (低 - 高)
     */
    public static int bytes2ToInt(byte[] bytes) {
        int number = bytes[0] & 0xFF;
        number |= ((bytes[1] << 8) & 0xFF00);// "|="按位或赋值。
        return number;
    }

    /**
     * Byte[4]数组转int
     * (低 - 高)
     */
    public static int bytes4ToInt(byte[] bytes) {
        int number = bytes[0] & 0xFF;
        number |= ((bytes[1] << 8) & 0xFF00);
        number |= ((bytes[2] << 16) & 0xFF0000);
        number |= ((bytes[3] << 24) & 0xFF000000);
        return number;
    }

    public static float bytesToFloat(byte[] bytes) {
        return Float.intBitsToFloat(bytes4ToInt(bytes));
    }

    /**
     * 16进制的转成10进制
     */
    public static int toInt(byte a) {
        int r = 0;
        r <<= 8;
        r |= (a & 0x000000ff);
        return r;
    }


    /**
     * 将16位byte[] 转换为32位String
     */
    public static String bytetoHex(byte buffer[]) {
        StringBuilder sb = new StringBuilder(buffer.length * 2);
        for (byte aBuffer : buffer) {
            sb.append(Character.forDigit((aBuffer & 240) >> 4, 16));
            sb.append(Character.forDigit(aBuffer & 15, 16));
        }
        return sb.toString();
    }

    /**
     * int 转byte数组 低位-高位
     */
    public static byte[] intToBytes4(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return b;
    }
}
