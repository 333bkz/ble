package www.ble.sixsix.core;

import www.ble.sixsix.base.IWriter;
import www.ble.sixsix.base.callback.IBleSendStateCallback;
import www.ble.sixsix.device.golf.CommandGenerator;

import static www.ble.sixsix.common.State.STATE_DEFAULT;
import static www.ble.sixsix.common.State.STATE_SEND_HEART_BEAT;

public final class SendCommUtil {
    //当前发送指令数据
    private static byte[] currentComm;
    //当前发送指令类型
    private static byte currentCommType;
    /***
     * 当前蓝牙发送状态
     * 当蓝牙无操作时必须改为STATE_DEFAULT --> 处理推送状态数据
     */
    private static int currentState = STATE_DEFAULT;

    private static IBleSendStateCallback stateCallback;

    //是否是默认状态
    public static boolean isDefaultState() {
        return currentState == STATE_DEFAULT;
    }

    public static byte[] getCurrentComm() {
        return currentComm;
    }

    public static byte getCurrentCommType() {
        return currentCommType;
    }

    //重置蓝牙状态
    public static void resetCurrentState() {
        setCurrentState(STATE_DEFAULT);
    }

    public static boolean currentStateIs(int state) {
        return currentState == state;
    }

    public static void setCurrentState(int state) {
        currentState = state;
        if (stateCallback != null) {
            stateCallback.onStateChange(currentState);
        }
    }

    public static int getCurrentState() {
        return currentState;
    }

    public static void setStateListener(IBleSendStateCallback _stateCallback) {
        stateCallback = _stateCallback;
    }

    /**
     * 发送心跳包
     */
    public static void sendHeartBeatComm(IWriter writer) {
//        currentComm = CommandGenerator.getHeartBeatProtocol();
//        setCurrentState(STATE_SEND_HEART_BEAT);
//        writer.writeCommand(currentComm, false, false);
    }


    public static void sendSyncSuccessComm(IWriter writer) {
        currentComm = CommandGenerator.getSyncSuccessProtocol();
        setCurrentState(STATE_SEND_HEART_BEAT);
        writer.writeCommand(currentComm, false, false);
    }
}
