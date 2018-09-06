package www.ble.sixsix.base.callback;

import www.ble.sixsix.exception.Exception;
import www.ble.sixsix.core.DeviceInfo;

/**
 * 连接设备回调
 */
public interface IConnectCallback {
    //连接成功
    void onConnectSuccess(DeviceInfo deviceInfo);

    //连接失败
    void onConnectFailure(DeviceInfo deviceInfo, Exception exception);

    /**
     * 连接断开
     *
     * @param isActive 是否主动断开
     */
    void onDisconnect(DeviceInfo deviceInfo, boolean isActive);
}
