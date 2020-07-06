package www.ble.sixsix.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.CallSuper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import www.ble.sixsix.Manager;
import www.ble.sixsix.base.IHandler;
import www.ble.sixsix.base.IWriter;
import www.ble.sixsix.base.callback.IBleCallback;
import www.ble.sixsix.base.callback.IConnectCallback;
import www.ble.sixsix.common.ConnState;
import www.ble.sixsix.exception.Exception;
import www.ble.sixsix.util.ConvertTool;
import www.ble.sixsix.util.LLLLog;
import www.ble.sixsix.util.ReflexTool;

import static www.ble.sixsix.common.BleConfig.MAX_AUTO_DISCONNECT_TIME;
import static www.ble.sixsix.common.BleConfig.MAX_CONNECT_TIME;
import static www.ble.sixsix.common.BleConfig.MAX_RECONNECT_TIMES;
import static www.ble.sixsix.common.BleConfig.MAX_SEND_HEART_BEAT_BLANK;
import static www.ble.sixsix.common.BleConfig.MSG_AUTO_DISCONNECT;
import static www.ble.sixsix.common.BleConfig.MSG_CONNECTED_COMMAND;
import static www.ble.sixsix.common.BleConfig.MSG_CONNECT_FAILURE_COMMAND;
import static www.ble.sixsix.common.BleConfig.MSG_DISCONNECT_COMMAND;
import static www.ble.sixsix.common.BleConfig.MSG_HEART_BEAT;
import static www.ble.sixsix.common.BleConfig.MSG_RESEND_COMMAND;
import static www.ble.sixsix.common.BleConfig.MSG_TIMEOUT_CONNECT;
import static www.ble.sixsix.common.BleConfig.MSG_TIMEOUT_SEND;
import static www.ble.sixsix.common.ConnState.CONNECTED;
import static www.ble.sixsix.common.ConnState.CONNECTING;
import static www.ble.sixsix.common.ConnState.DISCONNECTED;
import static www.ble.sixsix.common.ConnState.ERROR;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public abstract class Device implements IWriter {
    public static final String TAG = "[Device] -> ";
    protected BluetoothDevice device;//设备
    protected DeviceInfo deviceInfo;//设备信息 作为搜索返回
    protected BluetoothGatt gatt;
    protected Map<UUID, BluetoothGattService> services;
    protected Map<UUID, BluetoothGattCharacteristic> characteristics;
    protected Map<UUID, BluetoothGattDescriptor> descriptors;
    protected IConnectCallback connectCallback;//连接回调
    protected IBleCallback bleCallback;//数据操作回调
    protected IHandler dataHandler;

    private ConnState connState = DISCONNECTED;//连接状态
    private int reconnectTimes = 0;//当前蓝牙连接次数
    protected int resendTimes = 0;//当前发送指令次数
    private boolean isAlreadyCallback = false;//是否已回调断开连接状态
    protected boolean isActiveDisconnect = false;//是否主动断开连接
    protected boolean autoDisconnectBLE = false;//是否开启自动断开蓝牙
    protected boolean isLongConnection = false;//是否开启静默连接
    protected boolean isBindRequest = false;//是否是绑定连接请求
    protected boolean isSendHeartBeat = false;//是否发生心跳包

    protected final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIMEOUT_CONNECT://连接超时
                    LLLLog.w(TAG + " 连接超时 ");
                    reconnect();
                    break;
                case MSG_TIMEOUT_SEND://命令发送超时
                    LLLLog.w(TAG + " 命令发送超时 ");
                    resendCommandForTimeOut();
                    break;
                case MSG_AUTO_DISCONNECT://自动断开连接
                    LLLLog.w(TAG + " 自动断开连接 ");
                    disconnect();
                    break;
                case MSG_HEART_BEAT://发送心跳包
                    if (isConnected() && isSendHeartBeat) {
                        SendCommUtil.sendHeartBeatComm(Device.this);
                    } else {
                        stopHeartBeatTimer();
                    }
                    break;
                case MSG_RESEND_COMMAND:
                    resendCommandForTimeOut();
                    break;
                case MSG_CONNECTED_COMMAND:
                    if (connectCallback != null) {
                        connectCallback.onConnectSuccess(deviceInfo);
                    }
                    break;
                case MSG_DISCONNECT_COMMAND:
                    if (connectCallback != null) {
                        connectCallback.onDisconnect(deviceInfo, isActiveDisconnect);
                    }
                    break;
                case MSG_CONNECT_FAILURE_COMMAND:
                    if (connectCallback != null) {
                        connectCallback.onConnectFailure(deviceInfo, new Exception("the connection is abnormal"));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        private boolean setDefaultCharacteristic(BluetoothGatt _gatt) {
            clearService();
            List<BluetoothGattService> services = _gatt.getServices();
            return !services.isEmpty()
                    && Device.this.setDefaultCharacteristic(services);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt _gatt, int status, int newState) {
            LLLLog.i(TAG + "连接状态码 : " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        LLLLog.i(TAG + "连接状态 : connected ");
                        if (isConnecting()) {
                            //部分手机必须延迟比较长时间才能读写数据
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (isConnecting()) {
                                        if (gatt != null) {
                                            //搜索服务
                                            gatt.discoverServices();
                                        } else {
                                            //排除正在连接时解绑会出现的情况
                                            onDisconnected(false, true);
                                        }
                                    }
                                }
                            }, 2000);
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        LLLLog.i(TAG + "连接状态 : disconnected");
                        LLLLog.i(TAG + "是否主动断开 : " + isActiveDisconnect);
                        onDisconnected(false, isActiveDisconnect);
                        if (!isActiveDisconnect) {
                            if (isConnectError()) {//连接错误 已处理重连
                                LLLLog.i(TAG + "连接错误");
                            } else {
                                LLLLog.i(TAG + "被动断开连接");
                                reconnect();
                            }
                        }
                        break;
                    default:
                        break;
                }
            } else {
                //连接错误
                onConnectError();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt _gatt, int status) {
            LLLLog.e(TAG + "发现服务 状态码 : " + status);

            if (gatt == null) {
                return;// --> 连接超时
            }

            if (!isConnecting()) {
                return;// --> 连接超时
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                onConnectError();
                return;
            }

            //连接成功 -> 清除命令队列
            SendCommUtil.resetCurrentState();
            //Queue.clear();
            handler.removeMessages(MSG_TIMEOUT_SEND);
            handler.removeMessages(MSG_AUTO_DISCONNECT);

            if (!setDefaultCharacteristic(_gatt)) {
                LLLLog.e(TAG + "search Characteristic failed");
                return;// --> 连接超时
            }

            if (autoDisconnectBLE) {
                //开启主动断开连接计时
                handler.sendEmptyMessageDelayed(MSG_AUTO_DISCONNECT, MAX_AUTO_DISCONNECT_TIME);
            }

            isBindRequest = false;
            resetReconnectTimes();
            handler.removeMessages(MSG_TIMEOUT_CONNECT);

            if (!isConnected()) {//未知原因导致重复回调
                setConnState(CONNECTED);
                onConnectSuccess();
                handler.sendEmptyMessage(MSG_CONNECTED_COMMAND);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic c, int s) {
            super.onCharacteristicWrite(gatt, c, s);

            if (s != BluetoothGatt.GATT_SUCCESS) {
                LLLLog.e(TAG + "onCharacteristicWrite status : " + s);
            }

            if (autoDisconnectBLE) {
                //重置主动断开计时
                handler.removeMessages(MSG_AUTO_DISCONNECT);
                handler.sendEmptyMessageDelayed(MSG_AUTO_DISCONNECT, MAX_AUTO_DISCONNECT_TIME);
            }

            if (isSendHeartBeat
                    && isSendingHeartBeat(c.getValue())) {
                resumeHeartBeatTimer();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (characteristic != null
                    && characteristic.getValue() != null) {
                LLLLog.i(TAG + "receive : " + ConvertTool.bytesToHexString(characteristic.getValue()));
                if (getDataHandler() != null) {
                    getDataHandler().handleData(characteristic.getValue());
                }
            }
        }
    };

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getAddress() {
        return device == null ? "" : device.getAddress();
    }

    public String getName() {
        return device == null ? "" : device.getName();
    }

    public ConnState getConnectState() {
        return connState;
    }

    protected void setConnState(ConnState _connState) {
        if (deviceInfo != null) {
            LLLLog.i(TAG + deviceInfo.getName() + " -> setConnState -> " + _connState);
        }
        connState = _connState;
    }

    public boolean isConnected() {
        return connState == CONNECTED;
    }

    public boolean isConnectError() {
        return connState == ERROR;
    }

    public boolean isConnecting() {
        return connState == CONNECTING;
    }

    public void resetSendCommandTimes() {
        resendTimes = 0;
    }

    public void resetReconnectTimes() {
        reconnectTimes = 0;
    }

    //停止发送
    public void stopHeartBeatTimer() {
        handler.removeMessages(MSG_HEART_BEAT);
    }

    //恢复
    public void resumeHeartBeatTimer() {
        handler.sendEmptyMessageDelayed(MSG_HEART_BEAT, MAX_SEND_HEART_BEAT_BLANK);
    }

    //主动断开
    public void disconnectDevice() {
        disconnect();
        isBindRequest = false;
    }

    @CallSuper
    public void connectDevice(boolean _isBindRequest, IConnectCallback _connCallback) {
        isBindRequest = _isBindRequest;
        connectCallback = _connCallback;
        isLongConnection = true;
        isSendHeartBeat = false;
        isActiveDisconnect = false;

        if (!TextUtils.isEmpty(deviceInfo.getAddress())
                && deviceInfo.getAddress().contains(":")) {
            resetReconnectTimes();
            isAlreadyCallback = false;
            connect();
        }
    }

    private void disconnect() {
        removeMessages();

        isActiveDisconnect = true;

        if (gatt != null) {
            if (ReflexTool.isEffective(gatt)) {
                gatt.disconnect();
            } else {
                onDisconnected(true, true);
            }
        }
    }

    protected void removeMessages() {
        handler.removeMessages(MSG_TIMEOUT_CONNECT);
        handler.removeMessages(MSG_TIMEOUT_SEND);
        handler.removeMessages(MSG_AUTO_DISCONNECT);
        handler.removeMessages(MSG_HEART_BEAT);
    }

    private void connect() {
        stopHeartBeatTimer();
        LLLLog.i(TAG + "连接状态 ： " + connState);
        if (!isConnecting()) {
            device = Manager.getInstance().getBluetoothAdapter().getRemoteDevice(deviceInfo.getAddress());
            if (device != null) {
                close();
                //开启连接超时计时
                handler.sendEmptyMessageDelayed(MSG_TIMEOUT_CONNECT, MAX_CONNECT_TIME);
                setConnState(CONNECTING);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (device != null && isConnecting()) {
                            LLLLog.i(TAG + "开始连接");
                            //连接慢失败率高不推荐使用 gatt.connect()
                            Context context = Manager.getInstance().getContext();
                            if (context != null) {
                                gatt = device.connectGatt(context, false, gattCallback);
                            }
                        }
                    }
                }, 1000);
            }
        }
    }

    private void reconnect() {
        if (isBindRequest) {
            LLLLog.i(TAG + "绑定操作 不重连");
            disconnect();
            onDisconnected(false, true);
            return;
        }

        if (Manager.getInstance().checkBle()) {
            LLLLog.i(TAG + "当前重连次数 -> " + reconnectTimes);
            if (reconnectTimes < MAX_RECONNECT_TIMES) {
                reconnectTimes++;
                connect();
            } else {
                onDisconnected(false, true);
                longConnection();
            }
        } else {
            LLLLog.w(TAG + "系统蓝牙未开启");
            close();
            onDisconnected(true, true);
        }
    }

    private void longConnection() {
        LLLLog.i(TAG + "long connection ->" + isLongConnection + " state -> " + connState);
        if (isLongConnection) {
            resetReconnectTimes();
            reconnect();
        }
    }

    private void onDisconnected(boolean isConnectError, boolean ifCallback) {
        LLLLog.i(TAG + "onDisconnected -> " + isConnectError);
        setConnState(DISCONNECTED);
        LLLLog.i(TAG + "ifCallback -> " + ifCallback);
        if (ifCallback) {
            LLLLog.i(TAG + "isAlreadyCallback -> " + isAlreadyCallback);
            if (!isAlreadyCallback) {
                isAlreadyCallback = true;
                if (isConnectError) {
                    handler.sendEmptyMessage(MSG_CONNECT_FAILURE_COMMAND);
                } else {
                    handler.sendEmptyMessage(MSG_DISCONNECT_COMMAND);
                }
            }
        }
        clearConnectInfo();
        close();
    }

    protected void onConnectError() {
        handler.removeMessages(MSG_TIMEOUT_CONNECT);
        setConnState(ConnState.ERROR);
        if (gatt != null && ReflexTool.isEffective(gatt)) {
            gatt.disconnect();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isConnectError()) {
                        close();
                    }
                    reconnect();
                }
            }, 3000);
        } else {
            reconnect();
        }
    }

    public void sendDisconnectedState(boolean error) {
        isAlreadyCallback = true;
        if (error) {
            handler.sendEmptyMessage(MSG_CONNECT_FAILURE_COMMAND);
        } else {
            handler.sendEmptyMessage(MSG_DISCONNECT_COMMAND);
        }
        clearService();
    }

    protected void clearService() {
        if (services != null) {
            services.clear();
        }
        if (characteristics != null) {
            characteristics.clear();
        }
        if (descriptors != null) {
            descriptors.clear();
        }
    }

    public void clear() {
        clearConnectInfo();
        gatt = null;
        device = null;
        deviceInfo = null;
        bleCallback = null;
        connectCallback = null;
    }

    //清除连接信息
    public void clearConnectInfo() {
        removeMessages();
        Queue.clear();
        clearService();
        SendCommUtil.resetCurrentState();
        if (getDataHandler() != null) {
            getDataHandler().reset();
        }
    }

    private void close() {
        if (gatt != null) {
            LLLLog.i(TAG + "to close gatt ");
            gatt.disconnect();
            ReflexTool.refreshDeviceCache(gatt);
            if (gatt != null) {
                gatt.close();
                gatt = null;
            }
        }
    }

    /**
     * 重发命令
     * <p>发送命令超时或者发送命令失败时调用
     */
    private void resendCommandForTimeOut() {
        resendCommand();
    }

    /**
     * 订阅
     *
     * @param c      需订阅的特征
     * @param enable 打开/关闭
     */
    protected void subscribe(BluetoothGattCharacteristic c, boolean enable) {
        if (gatt != null) {
            gatt.setCharacteristicNotification(c, enable);
            List<BluetoothGattDescriptor> descriptorList = c.getDescriptors();
            if (descriptorList != null && descriptorList.size() > 0) {
                for (BluetoothGattDescriptor descriptor : descriptorList) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }
        }
    }

    public void setBleCallback(IBleCallback bleCallback) {
        this.bleCallback = bleCallback;
    }

    /**
     * 遍历服务获取特征
     */
    protected abstract boolean setDefaultCharacteristic(List<BluetoothGattService> _services);

    /**
     * 连接成功后续处理
     */
    protected abstract void onConnectSuccess();

    /**
     * 是否正在发送心跳包
     */
    protected boolean isSendingHeartBeat(byte[] value) {
        return true;
    }

    /**
     * 获取数据处理类
     */
    protected abstract IHandler getDataHandler();
}
