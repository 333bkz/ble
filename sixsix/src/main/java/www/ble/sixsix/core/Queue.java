package www.ble.sixsix.core;

import java.util.LinkedList;
import java.util.List;

import www.ble.sixsix.Manager;
import www.ble.sixsix.common.State;
import www.ble.sixsix.util.LLLLog;


public final class Queue {
    private static final String TAG = " Queue ";
    private static LinkedList<String> sQueue = new LinkedList<>();//指令集

    /**
     * 发送下一个请求,没有移除第一位数据
     */
    private static void sendNextCommand() {
        Manager.getInstance().getCurrentDevice().resumeHeartBeatTimer();
        if (sQueue.peek() != null) {
            switch (sQueue.peek()) {
                default:
                    LLLLog.w(TAG + " wrong --> remain queue item: " + sQueue.peek());
                    clear();
                    break;
            }
        } else {
            SendCommUtil.resetCurrentState();
            LLLLog.i(TAG + "commandList is empty stop ble send");
        }
    }

    /**
     * 添加命令 如果是第一次添加则开始发送命令
     */
    public static void setCommand(String comm) {
        if (Manager.getInstance().isConnected()) {//已连接
            if (checkSize()) {
                sQueue.add(comm);
                sendNextCommand();
            } else if (SendCommUtil.currentStateIs(State.STATE_SEND_HEART_BEAT)) {
                //该状态代表蓝牙空闲
                clear();
                sQueue.add(comm);
                sendNextCommand();
            } else {
                LLLLog.w(TAG + "currentState : " + SendCommUtil.getCurrentState());
            }
        } else {
            clear();
            Manager.getInstance().getCurrentDevice().sendDisconnectedState(false);
            LLLLog.w(TAG + " 设备未连接 ");
        }
    }

    /**
     * 添加命令集 如果是第一次添加则开始发送命令
     */
    public static void setCommandList(List<String> comm) {
        if (Manager.getInstance().isConnected()) {
            if (checkSize()) {
                sQueue.addAll(comm);
                sendNextCommand();
            } else if (SendCommUtil.currentStateIs(State.STATE_SEND_HEART_BEAT)) {
                //该状态代表蓝牙空闲
                clear();
                sQueue.addAll(comm);
                sendNextCommand();
            } else {
                LLLLog.w(TAG + "currentState : " + SendCommUtil.getCurrentState());
            }
        } else {
            clear();
            Manager.getInstance().getCurrentDevice().sendDisconnectedState(false);
            LLLLog.w(TAG + "设备未连接");
        }
    }

    private static boolean checkSize() {
        boolean isEmpty = sQueue.isEmpty();
        LLLLog.i(TAG + "queue isEmpty : " + isEmpty);
        if (!isEmpty) {
            int size = sQueue.size();
            if (size > 20) {
                LLLLog.w(TAG + "queue size error : " + size);
                clear();
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    /**
     * 移除第一位数据,发送下一个请求
     */
    public static void sendNextAndRemoveFirstCommand() {
        sQueue.poll();
        sendNextCommand();
    }

    public static void clear() {
        sQueue.clear();
    }
}
