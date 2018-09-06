package www.ble.sixsix.device.golf;

import www.ble.sixsix.util.ConvertTool;

@SuppressWarnings("WeakerAccess")
public class GolfData {

    /**
     * 检测状态
     * 0：未探测到
     * 1：初步探测到
     * 2：确定探测到
     */
    public int detectedStatus;

    /**
     * 最大挥杆速度
     */
    public long maxSwingSpeed;

    /**
     * 最大挥杆速度点与击球点的时间差
     */
    public int timeDifference;

    /**
     * 挥杆节拍：上杆时长与下杆时长的比值
     */
    public double pace;


    /**
     * 击球强度指数
     */
    public double strokeStrengthIndex;

    /**
     * 挥杆速度指数
     */
    public double swingSpeedIndex;

    /**
     * 面开度：最大的挥杆速度，单位与传感器传出值的单位相同
     */
    public double leftRightBias;


    /**
     * 击球角度
     */
    public double pitchAngle;

    /**
     * 挥杆平面指数
     */
    public double planeIndex;

    /**
     * 上杆动作与标准模板的相似度
     */
    public double simLift;


    /**
     * 下杆动作与标准模板的相似度
     */
    public double simSwingDown;

    /**
     * 全挥杆动作与标准模板的相似度
     */
    public double simSwingGolf;


    public GolfData(byte[] data) {
        detectedStatus = data[0];

        byte[] maxSwingSpeeds = new byte[4];
        System.arraycopy(data, 1, maxSwingSpeeds, 0, maxSwingSpeeds.length);
        maxSwingSpeed = ConvertTool.bytes4ToInt(maxSwingSpeeds);

        byte[] timeDifferences = new byte[2];
        System.arraycopy(data, 5, timeDifferences, 0, timeDifferences.length);
        timeDifference = ConvertTool.bytes2ToInt(timeDifferences);

        byte[] paces = new byte[4];
        System.arraycopy(data, 7, paces, 0, paces.length);
        pace = ConvertTool.bytesToFloat(paces);
    }

    public void assign2(byte[] data) {
        byte[] bytes = new byte[4];
        System.arraycopy(data, 0, bytes, 0, bytes.length);
        strokeStrengthIndex = ConvertTool.bytesToFloat(bytes);
        System.arraycopy(data, 4, bytes, 0, bytes.length);
        swingSpeedIndex = ConvertTool.bytesToFloat(bytes);
        System.arraycopy(data, 8, bytes, 0, bytes.length);
        leftRightBias = ConvertTool.bytesToFloat(bytes);
    }

    public void assign3(byte[] data) {
        byte[] bytes = new byte[4];
        System.arraycopy(data, 0, bytes, 0, bytes.length);
        pitchAngle = ConvertTool.bytesToFloat(bytes);
        System.arraycopy(data, 4, bytes, 0, bytes.length);
        planeIndex = ConvertTool.bytesToFloat(bytes);
        System.arraycopy(data, 8, bytes, 0, bytes.length);
        simLift = ConvertTool.bytesToFloat(bytes);
    }

    public void assign4(byte[] data) {
        byte[] bytes = new byte[4];
        System.arraycopy(data, 0, bytes, 0, bytes.length);
        simSwingDown = ConvertTool.bytesToFloat(bytes);
        System.arraycopy(data, 4, bytes, 0, bytes.length);
        simSwingGolf = ConvertTool.bytesToFloat(bytes);
    }

    @Override
    public String toString() {
        return "DATA: " +
                "\ndetectedStatus      = " + detectedStatus +
                "\nmaxSwingSpeed       = " + maxSwingSpeed +
                "\ntimeDifference      = " + timeDifference +
                "\npace                = " + pace +
                "\nstrokeStrengthIndex = " + strokeStrengthIndex +
                "\nswingSpeedIndex     = " + swingSpeedIndex +
                "\nleftRightBias       = " + leftRightBias +
                "\npitchAngle          = " + pitchAngle +
                "\nplaneIndex          = " + planeIndex +
                "\nsimLift             = " + simLift +
                "\nsimSwingDown        = " + simSwingDown +
                "\nsimSwingGolf        = " + simSwingGolf;
    }
}
