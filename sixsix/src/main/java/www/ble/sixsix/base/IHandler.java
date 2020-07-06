package www.ble.sixsix.base;

public interface IHandler {

    void handleData(byte[] data);

    void reset();
}
