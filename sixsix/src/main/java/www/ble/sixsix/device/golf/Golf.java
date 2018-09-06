package www.ble.sixsix.device.golf;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import www.ble.sixsix.base.IHandler;
import www.ble.sixsix.base.callback.IConnectCallback;
import www.ble.sixsix.common.BleConfig;
import www.ble.sixsix.common.State;
import www.ble.sixsix.common.UUID;
import www.ble.sixsix.core.Device;
import www.ble.sixsix.core.Queue;
import www.ble.sixsix.core.SendCommUtil;
import www.ble.sixsix.exception.Exception;
import www.ble.sixsix.util.ConvertTool;
import www.ble.sixsix.util.LLLLog;

import static www.ble.sixsix.common.BleConfig.MSG_RESEND_COMMAND;
import static www.ble.sixsix.common.BleConfig.MSG_TIMEOUT_SEND;

/**
 * 高尔夫球杆
 *
 * <p>每包数据最多20个字节，采样主动上传的方式，每次上传有4包数据，
 * 按照C1---C4顺序上传，C1-C3不需要回应，C4命令需要回应，如果没有回应，连续上传三次。
 * <p>注：1、校验和为所有数据累加值，如数据AA 0f 00 C1 02 07 8C 00 00 05 00 56 55 85 40 ；的校验和为0x384,。
 * 2、所有举例中的checksum 代表两个字节的校验和。
 * <p>数据传输格式见：蓝牙协议.png
 */
public final class Golf extends Device implements IDataHandlerCallback {
    public static final String TAG = "[Golf] -> ";
    private BluetoothGattCharacteristic mCharacteristic;//写

    @Override
    public void connectDevice(boolean _isBindRequest, IConnectCallback _connCallback) {
        super.connectDevice(_isBindRequest, _connCallback);
        isLongConnection = false;
    }

    @Override
    protected IHandler getDataHandler() {
        if (dataHandler == null)
            dataHandler = new GolfHandler(this);
        return dataHandler;
    }

    @Override
    protected boolean setDefaultCharacteristic(List<BluetoothGattService> _services) {
        for (BluetoothGattService service : _services) {
            if (service.getUuid().toString().toLowerCase().equals(UUID.SERVICE_UUID)) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic c : characteristics) {
                    String uuid = c.getUuid().toString().toLowerCase();
                    switch (uuid) {
                        case UUID.READ:
                            subscribe(c, true);
                            break;
                        case UUID.WRITE:
                            mCharacteristic = c;
                            break;
                    }
                }
                break;
            }
        }
        return mCharacteristic != null;
    }

    @Override
    protected void clearService() {
        super.clearService();
        mCharacteristic = null;
    }

    @Override
    protected void onConnectSuccess() {

    }

    @Override
    public void resendCommand() {
        LLLLog.i(TAG + "当前重发次数: " + resendTimes);
        if (resendTimes < BleConfig.MAX_RESEND_TIMES) {
            resendTimes++;

            switch (SendCommUtil.getCurrentState()) {
//                case :
//                    break;
                default:
                    //其他 发送当前请求
                    LLLLog.i(TAG + "send command time out —> " + SendCommUtil.getCurrentCommType());
                    writeCommand(SendCommUtil.getCurrentComm());
                    break;
            }
        } else {
            LLLLog.w(TAG + "重发超过限定次数!");
            SendCommUtil.resetCurrentState();
            resetSendCommandTimes();
            Queue.clear();
            if (getDataHandler() != null) {
                getDataHandler().reset();
            }
            bleCallback.onFailure(new Exception("send command time out"));
        }
    }

    @Override
    public void writeCommand(byte[] b) {
        writeCommand(b, true, true);
    }

    @Override
    public void writeCommand(byte[] b, boolean isOpenTimer, boolean ifResend) {
        writeCommand(mCharacteristic, b, isOpenTimer, ifResend);
    }

    @Override
    public void writeCommand(BluetoothGattCharacteristic c, byte[] b, boolean isOpenTimer, boolean ifResend) {
        if (!SendCommUtil.currentStateIs(State.STATE_SEND_HEART_BEAT)) {
            stopHeartBeatTimer();
        }

        if (gatt == null || c == null) {
            onConnectError();
            return;
        }

        synchronized (Device.class) {
            if (isOpenTimer) {
                handler.sendEmptyMessageDelayed(MSG_TIMEOUT_SEND, BleConfig.MAX_SEND_COMMAND_TIME);
            }

            c.setValue(b);
            boolean success = gatt.writeCharacteristic(c);

            LLLLog.i(TAG + "write value : " + ConvertTool.bytesToHexString(b) + " -> " + success);
            if (!success) {
                handler.removeMessages(MSG_TIMEOUT_SEND);
                if (ifResend) {
                    handler.sendEmptyMessageDelayed(MSG_RESEND_COMMAND, 50);
                }
            }
        }
    }

    @Override
    public void writeCommandDelay(byte[] b, long millisecond) {
        handler.sendEmptyMessageDelayed(MSG_RESEND_COMMAND, millisecond);
    }

    @Override
    public void handleIncorrectData() {
        //接收到错误的数据 -> 重发命令
//        resendCommand();
    }

    @Override
    public synchronized void handleGolfData(final GolfData data) {
        SendCommUtil.sendSyncSuccessComm(this);
        LLLLog.i(TAG + data.toString());
        if (bleCallback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    bleCallback.onSuccess(true, data);
                }
            });
        }
    }
}
