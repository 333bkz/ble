package www.ble.sixsix.util;

import android.bluetooth.BluetoothGatt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import www.ble.sixsix.core.Device;

public final class ReflexTool {

    /**
     * 清除缓存
     */
    public synchronized static void refreshDeviceCache(BluetoothGatt gatt) {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && gatt != null) {
                refresh.invoke(gatt);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * gatt 是否有效
     */
    public static boolean isEffective(BluetoothGatt gatt) {
        try {
            Field mService = BluetoothGatt.class.getDeclaredField("mService");
            mService.setAccessible(true);
            Object ojb = mService.get(gatt);
            if (null == ojb) {
                return false;
            }

            Field mClientIf = BluetoothGatt.class.getDeclaredField("mClientIf");
            mClientIf.setAccessible(true);
            int clientIf = mClientIf.getInt(gatt);
            if (0 == clientIf) {
                return false;
            }

            Field mConnState = BluetoothGatt.class.getDeclaredField("mConnState");
            mConnState.setAccessible(true);
            int connState = mConnState.getInt(gatt);

            /*
            connState :
             int CONN_STATE_IDLE = 0;
             int CONN_STATE_CONNECTING = 1;
             int CONN_STATE_CONNECTED = 2;
             int CONN_STATE_DISCONNECTING = 3;
             int CONN_STATE_CLOSED = 4;
             */
            LLLLog.i(Device.TAG + "this BluetoothGatt is effective");
//            LLLLog.i(Device.TAG + "gatt -> " + ojb.toString()
//                    + " clientIf -> " + clientIf
//                    + " connState -> " + connState);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
