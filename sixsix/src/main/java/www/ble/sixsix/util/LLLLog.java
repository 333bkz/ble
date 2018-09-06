package www.ble.sixsix.util;

import android.util.Log;

public final class LLLLog {
    private static final boolean LOG = true;
    private static final String TAG = "nnnnnn";


    public static void i(String msg) {
        if (LOG)
            Log.i(TAG, msg);
    }

    public static void e(String msg) {
        if (LOG)
            Log.e(TAG, msg);
    }

    public static void w(String msg) {
        if (LOG)
            Log.w(TAG, msg);
    }
}
