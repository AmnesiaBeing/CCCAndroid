package edu.cuc.ccc;

import android.util.ArraySet;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cuc.ccc.DeviceUtil.DeviceStatus;
import edu.cuc.ccc.DeviceUtil.DeviceType;
import edu.cuc.ccc.DeviceUtil.IPPortAddr;

/*
    描述设备信息的包，以JSON格式传输。
*/
public class Device {
    private final static String TAG = Device.class.getSimpleName();

    // for me
    public Device(String uuid, String name, DeviceType type, Certificate cert, PublicKey pubk, PrivateKey priv) {
        this.mUUID = uuid;
        this.mName = name;
        this.mType = type;
        this.mCertificate = cert;
        this.mPublicKey = pubk;
        this.mPrivateKey = priv;
    }

    // for new device from qrcode
    public Device(String uuid, String name, DeviceType type, String pin) {
        this.mUUID = uuid;
        this.mName = name;
        this.mType = type;
        this.mPIN = pin;
    }

    // for new device from nsd, ndef
    public Device(String uuid, String name, DeviceType type) {
        this.mUUID = uuid;
        this.mName = name;
        this.mType = type;
    }

    public Device() {
    }

    //----------------------------------------------------------------------------------------------

    // 20200218，又加了一堆内容，算是版本2吧
    private final static int ProtocolVersion = 2;

    // 协议版本号
    private int mProtocolVersion = ProtocolVersion;
    // 设备名称
    private String mName;
    // 设备ID
    private String mUUID;
    // 设备类型
    private DeviceUtil.DeviceType mType = DeviceUtil.DeviceType.Unknown;
    // 通过网络发现得到的IP地址与端口号
    private List<IPPortAddr> ipPortAddrs = new ArrayList<>();
    // TODO:通过二维码添加得到的物理信息
    //
    // 通过二维码扫描会有个PIN码
    private String mPIN;
    // 设备支持的插件
    private Set<String> mSupportFeatures = new ArraySet<>();
    // 设备配对状态
    private DeviceStatus mStatus = DeviceStatus.Unknown;
    // 设备证书
    private Certificate mCertificate;
    // 设备密钥（私钥只有自己知道）
    private PublicKey mPublicKey;
    private PrivateKey mPrivateKey;
    // 是否记录
    private boolean mRecorded = false;
    // 是否发现
    private boolean mDiscovered = false;

    //----------------------------------------------------------------------------------------------

    public int getProtocolVersion() {
        return mProtocolVersion;
    }

    public final void setProtocolVersion(int mProtocolVersion) {
        this.mProtocolVersion = mProtocolVersion;
    }

    public List<IPPortAddr> getIPPortAddress() {
        return ipPortAddrs;
    }

    public void addIPPortAddress(IPPortAddr addr) {
        ipPortAddrs.add(addr);
    }

    public void addIPPortAddresses(List<IPPortAddr> addrs) {
        ipPortAddrs.addAll(addrs);
    }

    public String getName() {
        return mName;
    }

    public void setName(String mDeviceName) {
        this.mName = mDeviceName;
    }

    public DeviceType getType() {
        return mType;
    }

    public void setType(DeviceType deviceType) {
        if (deviceType == null) deviceType = DeviceType.Unknown;
        this.mType = deviceType;
    }

    public boolean isPaired() {
        return this.mStatus == DeviceStatus.Paired;
    }

    public boolean isParing() {
        return this.mStatus == DeviceStatus.Pairing;
    }

    public boolean isRecorded() {
        return mRecorded;
    }

    public void setRecorded(boolean value) {
        mRecorded = value;
    }

    public boolean isDiscovered() {
        return mDiscovered;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        this.mUUID = UUID;
    }

    public Certificate getCertificate() {
        return mCertificate;
    }

    public void setCertificate(Certificate certificate) {
        this.mCertificate = certificate;
    }

    public PublicKey getPublicKey() {
        return mPublicKey;
    }

    public void setPublicKey(PublicKey key) {
        this.mPublicKey = key;
    }

    public PrivateKey getPrivateKey() {
        return mPrivateKey;
    }

    public void setPrivateKey(PrivateKey key) {
        this.mPrivateKey = key;
    }

    public Set<String> getSupportFeatures() {
        return mSupportFeatures;
    }

    public void addSupportFeature(String supportFeature) {
        this.mSupportFeatures.add(supportFeature);
    }

    public void addSupportFeatures(Set<String> supportFeatures) {
        this.mSupportFeatures.addAll(supportFeatures);
    }

    public DeviceStatus getStatus() {
        return mStatus;
    }

    public void setStatus(DeviceStatus status) {
        this.mStatus = status;
    }

    public String getPIN() {
        return mPIN;
    }

    public void setPIN(String PIN) {
        this.mPIN = PIN;
    }
}
