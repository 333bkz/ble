package www.ble.sixsix.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public final class DeviceInfo implements Parcelable {
    private String address;
    private String name;
    private int rssi;

    public DeviceInfo(String address, String name, int rssi) {
        this.address = address;
        this.name = name;
        this.rssi = rssi;
    }

    public String getAddress() {
        return TextUtils.isEmpty(address) ? "" : address.toUpperCase();
    }

    public void setAdd(String add) {
        this.address = add;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public DeviceInfo(Parcel in) {
        address = in.readString();
        name = in.readString();
        rssi = in.readInt();
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(name);
        dest.writeInt(rssi);
    }

    @Override
    public String toString() {
        return "address = " + address +
                ", name = " + name +
                ", rssi = " + rssi;
    }
}
