package edu.cuc.ccc;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

/*
    描述设备信息的包，以JSON格式传输。
*/
public class Device {
    private final static String TAG = Device.class.getSimpleName();

    private final static int ProtocolVersion = 1;

    // 协议版本号
    private int mProtocolVersion = ProtocolVersion;
    // 设备名称
    private String mDeviceName;
    // 设备类型
    private DeviceType mDeviceType = DeviceType.Unknown;
    //
//    private List<MACAddr> macAddrs;
    private List<IPAddr> ipAddrs;
    // 设备支持的功能（暂不实现）
//    private List<String> mSupportFeatures;
    private boolean isPaired = false;

    private String pairCode;

    public Device() {

    }

    public int getProtocolVersion() {
        return mProtocolVersion;
    }

    public final void setProtocolVersion(int mProtocolVersion) {
        this.mProtocolVersion = mProtocolVersion;
    }

    public List<IPAddr> getDeviceIPAddress() {
        return ipAddrs;
    }

    public IPAddr getDeviceCurrentIPAddress() {
        return ipAddrs.get(0);
    }

    public void setDeviceIPAddress(List<IPAddr> addrs) {
        ipAddrs = addrs;
    }

//    public List<MACAddr> getDeviceMACAddress() {
//        return macAddrs;
//    }

//    public void setDeviceMACAddress(List<MACAddr> mMACAddress) {
//        this.macAddrs = mMACAddress;
//    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public DeviceType getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        if (deviceType == null) deviceType = DeviceType.Unknown;
        this.mDeviceType = deviceType;
    }

//    public List<String> getSupportFeatures() {
//        return mSupportFeatures;
//    }
//
//    public void setSupportFeatures(List<String> mSupportFeatures) {
//        this.mSupportFeatures = mSupportFeatures;
//    }

    // 转换成json数据
    public static String toJSONStr(Device device) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Ver", device.getProtocolVersion());
            jsonObject.put("DevType", device.getDeviceType());
            jsonObject.put("DevName", device.getDeviceName());
            JSONArray jsonArray = new JSONArray();
            for (final IPAddr i : device.getDeviceIPAddress()) {
                jsonArray.put(new JSONObject() {{
                    put("addr", i.addr);
                    put("port", i.port);
                }});
            }
            jsonObject.put("DevIPAddr", jsonArray);
//            for (final MACAddr i : device.getDeviceMACAddress()) {
//                jsonArray.put(new JSONObject() {{
//                    put("type", i.type.name());
//                    put("addr", i.addr);
//                }});
//            }
//            jsonObject.put("DevMACAddr", jsonArray);
//        JSONArray jsonArray = new JSONArray();
//        for (String i : device.getSupportFeatures()) {
//            jsonArray.put(i);
//        }
//        jsonObject.put("DevFeatures", jsonArray);
        } catch (Exception e) {
            return "";
        }
        return jsonObject.toString();
    }

    @NotNull
    @Override
    public String toString() {
        return toJSONStr(this);
    }

    // 解析传入的json数据，如解析不成功返回null
    public static Device parseJSONStr(final String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            int ver = jsonObject.getInt("Ver");
            DeviceType devType;
            try {
                devType = DeviceType.valueOf(jsonObject.getString("DevType"));
            } catch (IllegalArgumentException e) {
                devType = DeviceType.Unknown;
            }
            String devName = jsonObject.getString("DevName");
//            List<MACAddr> addrs = new ArrayList<MACAddr>();
//            final JSONArray jsonArray = (JSONArray) jsonObject.get("DevMACAddr");
//            for (int i = 0; i < jsonArray.length(); i++) {
//                MACAddr macAddr = new MACAddr();
//                macAddr.type = MACAddrType.valueOf(jsonArray.getJSONObject(i).getString("type"));
//                macAddr.addr = jsonArray.getJSONObject(i).getString("addr");
//
//                addrs.add(macAddr);
//            }
//        List<String> features = new ArrayList<String>();
//        JSONArray jsonArray = (JSONArray) jsonObject.get("DevFeatures");
//        for (int i = 0; i < jsonArray.length(); i++) {
//            features.add(jsonArray.getJSONObject(i).getString(""));
//        }
            String pairCode = jsonObject.getString("PairCode");
            // 简单判断数据可靠性，有问题返回null
            if (ver != ProtocolVersion) throw new JSONException("版本号不匹配。");
            Device device = new Device();
            device.setProtocolVersion(ver);
            device.setDeviceType(devType);
            device.setDeviceName(devName);
            device.setPairCode(pairCode);
//            device.setDeviceMACAddress(addrs);
            return device;
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    public boolean isPaired() {
        return isPaired;
    }

    public void setPaired(boolean paired) {
        isPaired = paired;
    }

    public String getPairCode() {
        return pairCode;
    }

    public void setPairCode(String pairCode) {
        this.pairCode = pairCode;
    }

//    public enum MACAddrType {
//        // 无知者无畏
//        Unknown,
//        LAN,
//        WLAN,
//        BT,
//        // 低功耗蓝牙设备带宽太小，就暂时不考虑了
////        BLE
//    }

//    public static class MACAddr implements Serializable {
//        MACAddrType type;
//        String addr;
//    }

    public static class IPAddr implements Serializable {
        public InetAddress addr;
        public int port;

        public IPAddr(InetAddress addr, int port) {
            this.addr = addr;
            this.port = port;
        }
    }

    public enum DeviceType {
        Unknown,
        PC,
        Phone;

        public int getDrawableId() {
            switch (this) {
                case PC:
                    return R.drawable.ic_computer_black_24dp;
                case Phone:
                    return R.drawable.ic_smartphone_black_24dp;
                default:
                    return R.drawable.ic_help_black_24dp;
            }
        }
    }
}
