package www.ble.sixsix.common;

public interface BleConfig {
    /**
     * 默认蓝牙传输间隔(ms)
     */
    int DEFAULT_CONNECTION_BLANK = 15;
    /**
     * 设备最大连接次数
     */
    int MAX_RECONNECT_TIMES = 3;
    /**
     * 最大重发指令次数
     */
    int MAX_RESEND_TIMES = 5;
    /**
     * 最大设备搜索时间
     */
    int MAX_SCAN_TIME = 12 * 1000;
    /**
     * 最大连接蓝牙时间
     */
    int MAX_CONNECT_TIME = 25 * 1000;
    /**
     * 发送指令等待时间
     */
    int MAX_SEND_COMMAND_TIME = 1000;
    /**
     * 自动断开蓝牙时间
     */
    int MAX_AUTO_DISCONNECT_TIME = 5 * 60 * 1000;
    /**
     * 最大空闲时间
     */
    int MAX_FREE_TIME = 5 * 1000;
    /**
     * 心跳包间隔
     */
    int MAX_SEND_HEART_BEAT_BLANK = 800;


    /**
     * 连接超时
     */
    int MSG_TIMEOUT_CONNECT = 0x011;
    /**
     * 发送命令超时
     */
    int MSG_TIMEOUT_SEND = 0x012;
    /**
     * 自动断开连接
     */
    int MSG_AUTO_DISCONNECT = 0x013;
    /**
     * 心跳包
     */
    int MSG_HEART_BEAT = 0x014;
    /**
     * 停止扫描
     */
    int MSG_STOP_SCAN = 0x016;
    /**
     * 命令发送返回false-> 延迟短暂时间重发
     */
    int MSG_RESEND_COMMAND = 0x020;

    int MSG_CONNECTED_COMMAND = 0x030;
    int MSG_DISCONNECT_COMMAND = 0x031;
    int MSG_CONNECT_FAILURE_COMMAND = 0x032;
}
