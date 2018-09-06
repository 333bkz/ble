package www.ble.sixsix.base.callback;

import www.ble.sixsix.core.DeviceInfo;

/**
 * 扫描回调
 */
public interface IScanCallback {
    void onScan(DeviceInfo _device, int rssi);

    DeviceInfo onFilter(String _address, String _name, int _rssi);

    void cancel();
}
