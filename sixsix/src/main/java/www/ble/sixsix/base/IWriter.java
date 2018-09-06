package www.ble.sixsix.base;

import android.bluetooth.BluetoothGattCharacteristic;

public interface IWriter {

    /**
     * 重发命令
     */
    void resendCommand();

    /**
     * 发送命令
     */
    void writeCommand(byte[] b);

    /**
     * 发送命令
     *
     * @param b           数据
     * @param isOpenTimer 是否开启计时器
     * @param ifResend    是否允许重发
     */
    void writeCommand(byte[] b, boolean isOpenTimer, boolean ifResend);

    /**
     * 发送命令
     *
     * @param characteristic
     * @param b              数据
     * @param isOpenTimer    是否开启计时器
     * @param ifResend       是否允许重发
     */
    void writeCommand(BluetoothGattCharacteristic characteristic, byte[] b, boolean isOpenTimer, boolean ifResend);

    /**
     * 发送命令
     */
    void writeCommandDelay(byte[] b, long millisecond);
}
