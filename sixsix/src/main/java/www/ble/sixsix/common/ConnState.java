package www.ble.sixsix.common;

/**
 * 连接状态
 */
public enum ConnState {
    CONNECTING(0x00),    //连接中
    CONNECTED(0x01),     //已连接
    DISCONNECTED(0x05),    //断开
    ERROR(0x06);    //连接错误

    private int code;

    ConnState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
