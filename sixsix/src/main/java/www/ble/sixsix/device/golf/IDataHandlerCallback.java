package www.ble.sixsix.device.golf;

public interface IDataHandlerCallback {

    /**
     * 接收到错误数据后续处理
     */
    void handleIncorrectData();

    /**
     * 处理正确完整的数据
     */
    void handleGolfData(GolfData data);
}
