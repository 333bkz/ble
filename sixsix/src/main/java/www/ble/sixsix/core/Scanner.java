package www.ble.sixsix.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.util.Set;

import www.ble.sixsix.Manager;
import www.ble.sixsix.base.callback.IScanCallback;
import www.ble.sixsix.common.BleConfig;
import www.ble.sixsix.util.LLLLog;

import static www.ble.sixsix.common.BleConfig.MSG_STOP_SCAN;

public final class Scanner implements BluetoothAdapter.LeScanCallback {
    private IScanCallback scanCall;
    private boolean isScanning = false;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_STOP_SCAN:
                    stopScan();
                    break;
            }
        }
    };

    public void setCallback(IScanCallback _scanCall) {
        scanCall = _scanCall;
    }

    public void clear() {
        scanCall = null;
    }

    public void startScan() {
        if (Manager.getInstance().checkBle()) {
            if (!isScanning) {
                //已配对的设备在有的手机上不能被搜索到
                Set<BluetoothDevice> devices = Manager.getInstance().getBluetoothAdapter().getBondedDevices();
                for (BluetoothDevice device : devices) {
                    scanCall(device, -1);
                }

                LLLLog.i("[Scanner] -> start scan.");
                isScanning = true;
                Manager.getInstance().getBluetoothAdapter().startLeScan(this);
                handler.sendEmptyMessageDelayed(MSG_STOP_SCAN, BleConfig.MAX_SCAN_TIME);
            } else {
                LLLLog.i("[Scanner] -> already start scan.");
            }
        }
    }

    public void stopScan() {
        if (isScanning) {
            isScanning = false;
            handler.removeCallbacksAndMessages(null);
            Manager.getInstance().getBluetoothAdapter().stopLeScan(this);
            LLLLog.i("[Scanner] -> scan finish");

            if (scanCall != null) {
                scanCall.cancel();
                scanCall = null;
            }
        } else {
            LLLLog.i("[Scanner] -> scan has stopped.");
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!isScanning) {
            stopScan();
        }

        if (scanCall != null
                && device != null) {
            scanCall(device, rssi);
        }
    }

    private void scanCall(BluetoothDevice device, int rssi) {
        String name = device.getName();
        String address = device.getAddress();

        if (!TextUtils.isEmpty(name)
                && !TextUtils.isEmpty(address)) {
            if (scanCall != null) {
                DeviceInfo deviceInfo = scanCall.onFilter(address, name, rssi);
                if (deviceInfo != null) {
                    LLLLog.i("[Scanner] -> device -> " + name + " rssi -> " + rssi);
                    scanCall.onScan(deviceInfo, rssi);
                }
            }
        }
    }
}