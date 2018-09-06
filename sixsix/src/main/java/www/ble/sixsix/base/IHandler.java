package www.ble.sixsix.base;

import android.support.annotation.NonNull;

public interface IHandler {

    void handleData(@NonNull byte[] data);

    void reset();
}
