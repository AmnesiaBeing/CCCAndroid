package edu.cuc.ccc.packets;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
    描述设备信息的包，以JSON格式传输。
*/
public class DeviceInfo implements Parcelable {
    private static String TAG = DeviceInfo.class.getSimpleName();

    private static int ProtocolVersion = 1;

    // 协议版本号
    private int mProtocolVersion;
    // 设备名称
    private String mDeviceName;
    // 设备类型
    private DeviceType mDeviceType;
    //
    private List<MACAddr> macAddrs;

    // 设备支持的功能（暂不实现）
//    private List<String> mSupportFeatures;

    public DeviceInfo() {

    }

    protected DeviceInfo(Parcel in) {
        mProtocolVersion = in.readInt();
        mDeviceName = in.readString();
        mDeviceType = DeviceType.valueOf(in.readString());
        in.readList(macAddrs, MACAddr.class.getClassLoader());
//        mSupportFeatures = in.createStringArrayList();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mProtocolVersion);
        dest.writeString(mDeviceName);
        dest.writeString(mDeviceType.name());
        dest.writeList(macAddrs);
//        dest.writeStringList(mSupportFeatures);
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

    public int getProtocolVersion() {
        return mProtocolVersion;
    }

    public void setProtocolVersion(int mProtocolVersion) {
        this.mProtocolVersion = mProtocolVersion;
    }

    public List<MACAddr> getDeviceMACAddress() {
        return macAddrs;
    }

    public void setDeviceMACAddress(List<MACAddr> mMACAddress) {
        this.macAddrs = mMACAddress;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public DeviceType getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(DeviceType mDeviceType) {
        this.mDeviceType = mDeviceType;
    }

//    public List<String> getSupportFeatures() {
//        return mSupportFeatures;
//    }
//
//    public void setSupportFeatures(List<String> mSupportFeatures) {
//        this.mSupportFeatures = mSupportFeatures;
//    }

    // 转换成json数据
    public static String toJSONStr(DeviceInfo deviceInfo) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Ver", deviceInfo.getProtocolVersion());
            jsonObject.put("DevType", deviceInfo.getDeviceType());
            jsonObject.put("DevName", deviceInfo.getDeviceName());
            JSONArray jsonArray = new JSONArray();
            for (final MACAddr i : deviceInfo.getDeviceMACAddress()) {
                jsonArray.put(new JSONObject() {{
                    put("type", i.type.name());
                    put("addr", i.addr);
                }});
            }
            jsonObject.put("DevMACAddr", jsonArray);
//        JSONArray jsonArray = new JSONArray();
//        for (String i : deviceInfo.getSupportFeatures()) {
//            jsonArray.put(i);
//        }
//        jsonObject.put("DevFeatures", jsonArray);
        } catch (Exception e) {
            return "";
        }
        return jsonObject.toString();
    }

    @Override
    public String toString() {
        return toJSONStr(this);
    }

    // 解析传入的json数据，如解析不成功返回null
    public static DeviceInfo parseJSONStr(final String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            int ver = (int) jsonObject.getInt("Ver");
            DeviceType devType;
            try {
                devType = DeviceType.valueOf(jsonObject.getString("DevType"));
            } catch (IllegalArgumentException e) {
                devType = DeviceType.Unknown;
            }
            String devName = (String) jsonObject.get("DevName");
            List<MACAddr> addrs = new ArrayList<MACAddr>();
            final JSONArray jsonArray = (JSONArray) jsonObject.get("DevMACAddr");
            for (int i = 0; i < jsonArray.length(); i++) {
                MACAddr macAddr = new MACAddr();
                macAddr.type = MACAddrType.valueOf(jsonArray.getJSONObject(i).getString("type"));
                macAddr.addr = jsonArray.getJSONObject(i).getString("addr");

                addrs.add(macAddr);
            }
//        List<String> features = new ArrayList<String>();
//        JSONArray jsonArray = (JSONArray) jsonObject.get("DevFeatures");
//        for (int i = 0; i < jsonArray.length(); i++) {
//            features.add(jsonArray.getJSONObject(i).getString(""));
//        }
            // 简单判断数据可靠性，有问题返回null
            if (ver != ProtocolVersion) throw new JSONException("版本号不匹配。");
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setProtocolVersion(ver);
            deviceInfo.setDeviceType(devType);
            deviceInfo.setDeviceName(devName);
            deviceInfo.setDeviceMACAddress(addrs);
            return deviceInfo;
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    public enum MACAddrType {
        // 无知者无畏
        Unknown,
        LAN,
        WLAN,
        BT,
        // 低功耗蓝牙设备带宽太小，就暂时不考虑了
//        BLE
    }

    public static class MACAddr implements Serializable {
        MACAddrType type;
        String addr;
    }

    public enum DeviceType {
        Unknown,
        PC,
        Phone
    }
}
