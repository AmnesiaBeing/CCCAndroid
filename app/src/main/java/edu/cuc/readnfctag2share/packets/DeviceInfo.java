package edu.cuc.readnfctag2share.packets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/*
    描述设备信息的包，以JSON格式传输。
*/
public class DeviceInfo {
    private static String TAG = DeviceInfo.class.getSimpleName();

    private static int ProtocolVersion = 1;

    public static int DEVICE_TYPE_PHONE = 0;
    public static int DEVICE_TYPE_PC = 1;

    private int mProtocolVersion;
    private String mDeviceMACAddress;
    private String mDeviceName;
    private int mDeviceType;
    private List<String> mSupportFeatures;

    public int getProtocolVersion() {
        return mProtocolVersion;
    }

    public void setProtocolVersion(int mProtocolVersion) {
        this.mProtocolVersion = mProtocolVersion;
    }

    public String getDeviceMACAddress() {
        return mDeviceMACAddress;
    }

    public void setDeviceMACAddress(String mMACAddress) {
        this.mDeviceMACAddress = mMACAddress;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public int getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(int mDeviceType) {
        this.mDeviceType = mDeviceType;
    }

    public List<String> getSupportFeatures() {
        return mSupportFeatures;
    }

    public void setSupportFeatures(List<String> mSupportFeatures) {
        this.mSupportFeatures = mSupportFeatures;
    }

    public static String toJSONStr(DeviceInfo deviceInfo) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Ver", deviceInfo.getProtocolVersion());
        jsonObject.put("DevType", deviceInfo.getDeviceType());
        jsonObject.put("DevName", deviceInfo.getDeviceName());
        jsonObject.put("DevMACAddr", deviceInfo.getDeviceMACAddress());
        JSONArray jsonArray = new JSONArray();
        for (String i : deviceInfo.getSupportFeatures()) {
            jsonArray.put(i);
        }
        jsonObject.put("DevFeatures", jsonArray);
        return jsonObject.toString();
    }

    public static DeviceInfo parseJSONStr(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        int ver = (int) jsonObject.get("Ver");
        if (ver != ProtocolVersion) throw new JSONException("版本号不匹配。");
        int devType = (int) jsonObject.get("DevType");
        if (devType < 0 || devType > 1) throw new JSONException("无法识别的设备类型。");
        String devName = (String) jsonObject.get("DevName");
        String devMACAddr = (String) jsonObject.get("DevMACAddr");
        // if(!validMACADDR(devMACAddr))throw new JSONException("非法MAC地址。");
        List<String> features = new ArrayList<String>();
        JSONArray jsonArray = (JSONArray) jsonObject.get("DevFeatures");
        for (int i = 0; i < jsonArray.length(); i++) {
            features.add(jsonArray.getJSONObject(i).getString(""));
        }
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setProtocolVersion(ver);
        deviceInfo.setDeviceType(devType);
        deviceInfo.setDeviceName(devName);
        deviceInfo.setDeviceMACAddress(devMACAddr);
        return deviceInfo;
    }
}
