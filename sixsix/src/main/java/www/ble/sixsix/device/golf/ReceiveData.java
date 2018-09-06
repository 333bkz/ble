package www.ble.sixsix.device.golf;

public class ReceiveData {
    /**
     * 帧头
     */
    public byte head;

    /**
     * 数据长度
     */
    public byte length;

    /**
     * 帧编号
     */
    public byte frameNo;

    /**
     * 命令域
     */
    public byte command;

    /**
     * 数据域
     */
    public byte[] data;

    /**
     * 校验位
     */
    public byte[] check;

    public ReceiveData(byte[] data) {
        final int size = data.length;
        this.head = data[0];
        this.length = data[1];
        this.frameNo = data[2];
        this.command = data[3];
        this.check = new byte[2];
        this.check[0] = data[size - 2];
        this.check[1] = data[size - 1];

        if (size - 6 > 0) {
            this.data = new byte[size - 6];
            System.arraycopy(data, 4, this.data, 0, this.data.length);
        }
    }
}
