package www.ble.sixsix.base.callback;

import www.ble.sixsix.device.golf.GolfData;
import www.ble.sixsix.exception.Exception;

/**
 * 操作数据回调
 */
public interface IBleCallback {
    void onSuccess(boolean success, GolfData data);

    void onFailure(Exception exception);
}
