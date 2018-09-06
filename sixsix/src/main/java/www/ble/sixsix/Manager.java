package www.ble.sixsix;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import www.ble.sixsix.base.callback.IBleCallback;
import www.ble.sixsix.base.callback.IBleSendStateCallback;
import www.ble.sixsix.base.callback.IConnectCallback;
import www.ble.sixsix.base.callback.IScanCallback;
import www.ble.sixsix.common.ConnState;
import www.ble.sixsix.core.Device;
import www.ble.sixsix.core.DeviceInfo;
import www.ble.sixsix.core.Queue;
import www.ble.sixsix.core.Scanner;
import www.ble.sixsix.core.SendCommUtil;
import www.ble.sixsix.util.LLLLog;

public final class Manager {
    private volatile static Manager sInstance;
    private WeakReference<Context> mContext;
    private BluetoothManager mBluetoothManager;//蓝牙管理
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
    private Device mDevice;//当前操作设备
    private Scanner mScanner;

    public static Manager getInstance() {
        if (sInstance == null) {
            synchronized (Manager.class) {
                if (sInstance == null) {
                    sInstance = new Manager();
                }
            }
        }
        return sInstance;
    }

    private Manager() {

    }

    public void init(Context context) {
        if (mContext == null
                && context != null) {
            mContext = new WeakReference<>(context.getApplicationContext());
            mBluetoothManager = (BluetoothManager) mContext.get().getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
            }
        }
    }

    public void startScan(IScanCallback scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("This ScanCallback is Null!");
        }

        if (checkBle()) {
            if (mScanner == null) {
                mScanner = new Scanner();
            }
            mScanner.setCallback(scanCallback);
            mScanner.startScan();
        } else {
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.enable();
            }
            LLLLog.e("[Manager] -> check ble false.");
        }
    }

    public void stopScan() {
        if (mScanner != null) {
            mScanner.stopScan();
        }
    }

    public boolean connectDevice(DeviceInfo deviceInfo,
                                 boolean isBindRequest,
                                 IConnectCallback connectCallback,
                                 Class<? extends Device> clz) {

        if (deviceInfo == null
                || TextUtils.isEmpty(deviceInfo.getAddress())
                || connectCallback == null) {
            return false;
        }

        if (mDevice != null
                && mDevice.getAddress().equals(deviceInfo.getAddress())) {
            if (mDevice.isConnected()) {
                //已连接
                LLLLog.i("已连接");
                return false;
            }
            mDevice.clearConnectInfo();
        }

        try {
            mDevice = clz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (mDevice != null) {
            mDevice.setDeviceInfo(deviceInfo);
            mDevice.connectDevice(isBindRequest, connectCallback);
            return true;
        }

        return false;
    }

    public void setBleCallback(IBleCallback callback) {
        if (mDevice != null) {
            mDevice.setBleCallback(callback);
        }
    }

    public void setBleSendStateCallback(IBleSendStateCallback callback) {
        SendCommUtil.setStateListener(callback);
    }

    /**
     * 获取连接池中的设备
     */
    public Device getCurrentDevice() {
        return mDevice;
    }

    /**
     * 获取设备连接状态
     */
    public ConnState getConnectState() {
        if (mDevice != null) {
            return mDevice.getConnectState();
        }
        return ConnState.DISCONNECTED;
    }

    public boolean isConnected() {
        return mDevice != null
                && mDevice.isConnected();
    }

    /**
     * 断开设备
     */
    public void disconnect() {
        if (mDevice != null) {
            mDevice.disconnectDevice();
        }
    }

    /**
     * 重启
     */
    public void reStartBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.enable();
                }
            }, 500);
        }
    }

    /**
     * 清除命令队列
     */
    public void clearCommandQueue() {
        Queue.clear();
    }

    /**
     * 添加命令到队列中
     * 如果命令队列空闲则立即发送该命令
     */
    public void addCommands(List<String> comm) {
        Queue.setCommandList(comm);
    }

    /**
     * 添加命令到队列中
     * 如果命令队列空闲则立即发送该命令
     */
    public void addCommand(String comm) {
        Queue.setCommand(comm);
    }

    /**
     * 清除资源，在退出应用时调用
     */
    public void clear() {
        if (mDevice != null) {
            mDevice.clear();
            mDevice = null;
        }
        SendCommUtil.setStateListener(null);

        if (mScanner != null) {
            mScanner.clear();
        }
    }

    public @Nullable Context getContext() {
        return mContext.get();
    }

    public BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public boolean checkBle() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
}