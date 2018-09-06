package www.ble.sixsix.common;

/**
 * 蓝牙发送状态
 */
public interface State {
    /**
     * 当前蓝牙发送状态 无操作
     */
    int STATE_DEFAULT = 10000;
    /**
     * 当前蓝牙发送状态 查询设备状态
     */
    int STATE_SEND_QUERY_STATE = 10006;

    /**
     * 当前蓝牙状态发送心跳包
     */
    int STATE_SEND_HEART_BEAT = 30000;
}
