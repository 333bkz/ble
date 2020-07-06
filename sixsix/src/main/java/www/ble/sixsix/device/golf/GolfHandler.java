package www.ble.sixsix.device.golf;

import www.ble.sixsix.Manager;
import www.ble.sixsix.base.IHandler;
import www.ble.sixsix.util.ConvertTool;

public class GolfHandler implements IHandler {

    private IDataHandlerCallback callback;
    private GolfData data;

    public GolfHandler(IDataHandlerCallback _callback) {
        callback = _callback;
    }

    @Override
    public void handleData(byte[] data) {
        //数据长度不对
        if (data.length < 6) {
            handleIncorrectData();
            return;
        }

        //数据头不是以AA开头
        if (data[0] != -86) {
            handleIncorrectData();
            return;
        }

        if (checkDataCheckBit(data)) {
            Manager.getInstance().getCurrentDevice().resetSendCommandTimes();
            handleCorrectData(new ReceiveData(data));
        } else {
            handleIncorrectData();
        }
    }

    /**
     * 校验数据校验位
     */
    private boolean checkDataCheckBit(byte[] data) {
        //数据域长度位
        byte length = data[1];
        //数据总长度位
        byte totalLength = (byte) (length + 2);

        if (totalLength < 0) {
            totalLength += 128;
        }

        if (data.length != totalLength) {
            return false;
        }

        byte[] check = new byte[2];
        check[0] = data[totalLength - 2];
        check[1] = data[totalLength - 1];
        int checkTmp = 0;
        for (int i = 0; i < totalLength - 2; i++) {
            checkTmp += ConvertTool.toInt(data[i]);
        }
        //总数据校验位是否正确
        return ConvertTool.bytes2ToInt(check) == checkTmp;
    }

    /**
     * 处理校验错误的数据
     */
    private void handleIncorrectData() {
        data = null;//放弃该次传输的数据
        if (callback != null) {
            callback.handleIncorrectData();
        }
    }

    /**
     * 处理校验正确的数据
     *
     * @param receiveData 数据
     */
    private void handleCorrectData(ReceiveData receiveData) {
        switch (receiveData.command) {
            case CommandBit.C1:
                data = new GolfData(receiveData.data);
                break;
            case CommandBit.C2:
                if (data != null) {
                    data.assign2(receiveData.data);
                }
                break;
            case CommandBit.C3:
                if (data != null) {
                    data.assign3(receiveData.data);
                }
                break;
            case CommandBit.C4:
                if (data != null) {
                    data.assign4(receiveData.data);
                    callback.handleGolfData(data);
                }
                break;
        }
    }

    @Override
    public void reset() {
        data = null;
    }
}
