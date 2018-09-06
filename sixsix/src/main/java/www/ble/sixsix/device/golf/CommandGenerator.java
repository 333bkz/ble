package www.ble.sixsix.device.golf;


import www.ble.sixsix.util.ConvertTool;

public final class CommandGenerator {

    public static byte[] getSyncSuccessProtocol() {
        byte[] tempProtocol = new byte[]{0x55, 0x05, 0x00, CommandBit.C4, 0x01, 0x00, 0x00};
        int checksum = tempProtocol[0]
                + tempProtocol[1]
                + tempProtocol[2]
                + tempProtocol[3]
                + tempProtocol[4];
        byte[] sums = ConvertTool.intToBytes4(checksum);
        tempProtocol[5] = sums[0];
        tempProtocol[6] = sums[1];
        return tempProtocol;
    }
}
