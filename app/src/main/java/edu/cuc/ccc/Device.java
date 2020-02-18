package edu.cuc.ccc;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import edu.cuc.ccc.DeviceUtil.DeviceStatus;
import edu.cuc.ccc.DeviceUtil.DeviceType;
import edu.cuc.ccc.DeviceUtil.IPPortAddr;

/*
    描述设备信息的包，以JSON格式传输。
*/
public class Device {
    private final static String TAG = Device.class.getSimpleName();

    public Device() {
    }

    public Device(String uuid, String name, DeviceType type, String cert, String pubKey, String priKey) {
        this.mUUID = uuid;
        this.mDeviceName = name;
        this.mDeviceType = type;
        this.mCertificate = cert;
        this.mPublicKey = pubKey;
        this.mPrivateKey = priKey;
    }

    //----------------------------------------------------------------------------------------------

    @NotNull
    @Override
    public String toString() {
        return DeviceUtil.toJSONStr(this);
    }

    //----------------------------------------------------------------------------------------------

    // 20200218，又加了一堆内容，算是版本2吧
    private final static int ProtocolVersion = 2;

    // 协议版本号
    private int mProtocolVersion = ProtocolVersion;
    // 设备名称
    private String mDeviceName;
    // 设备ID
    private String mUUID;
    // 设备类型
    private DeviceUtil.DeviceType mDeviceType = DeviceUtil.DeviceType.Unknown;
    // 通过网络发现得到的IP地址与端口号
    private List<IPPortAddr> ipPortAddrs = new ArrayList<>();
    // TODO:通过二维码添加得到的物理信息
    //
    // 设备支持的插件
    private List<String> mSupportFeatures = new ArrayList<>();
    // 设备配对状态
    private DeviceStatus mStatus = DeviceStatus.Unknown;
    // 设备证书
    private String mCertificate;
    // 设备密钥（私钥只有自己知道）
    private String mPublicKey;
    private String mPrivateKey;

    //----------------------------------------------------------------------------------------------

    public int getProtocolVersion() {
        return mProtocolVersion;
    }

    public final void setProtocolVersion(int mProtocolVersion) {
        this.mProtocolVersion = mProtocolVersion;
    }

    public List<IPPortAddr> getDeviceIPPortAddress() {
        return ipPortAddrs;
    }

    public void addDeviceIPPortAddress(IPPortAddr addr) {
        ipPortAddrs.add(addr);
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

    public void setDeviceType(DeviceType deviceType) {
        if (deviceType == null) deviceType = DeviceType.Unknown;
        this.mDeviceType = deviceType;
    }

    public boolean isPaired() {
        return this.mStatus == DeviceStatus.Paired;
    }

    public boolean isParing() {
        return this.mStatus == DeviceStatus.Pairing;
    }

//    public boolean isRecorded() {
//        return ((this.mStatus == DeviceStatus.Recorded_Discovered) ||
//                (this.mStatus == DeviceStatus.Recorded_but_not_Discovered) ||
//                (this.mStatus == DeviceStatus.Pairing) ||
//                (this.mStatus == DeviceStatus.Paired));
//    }
//
//    public boolean isDiscovered() {
//        return ((this.mStatus == DeviceStatus.Recorded_Discovered) ||
//                (this.mStatus == DeviceStatus.Discovered_but_not_Recorded) ||
//                (this.mStatus == DeviceStatus.Pairing) ||
//                (this.mStatus == DeviceStatus.Paired));
//    }

    public String getDeviceUUID() {
        return mUUID;
    }

    public String getCertificate() {
        return mCertificate;
    }

    public void setCertificate(String certificate) {
        this.mCertificate = certificate;
    }

    public String getPublicKey() {
        return mPublicKey;
    }

    public void setPublicKey(String key) {
        this.mPublicKey = key;
    }

    public String getPrivateKey() {
        return mPrivateKey;
    }

    public void setPrivateKey(String key) {
        this.mPrivateKey = key;
    }

    public List<String> getSupportFeatures() {
        return mSupportFeatures;
    }

    public void addSupportFeatures(String supportFeatures) {
        this.mSupportFeatures.add(supportFeatures);
    }

    public DeviceStatus getStatus() {
        return mStatus;
    }

    public void setStatus(DeviceStatus status) {
        this.mStatus = status;
    }

    public void setUUID(String UUID) {
        this.mUUID = UUID;
    }
}
