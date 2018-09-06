package www.ble.sixsix.exception;

import java.io.Serializable;

import www.ble.sixsix.util.LLLLog;

public final class Exception implements Serializable {

    private String exception;

    public Exception(String exception) {
        this.exception = exception;
    }

    public void print() {
        LLLLog.e(exception);
    }

    public String getException() {
        return exception;
    }
}
