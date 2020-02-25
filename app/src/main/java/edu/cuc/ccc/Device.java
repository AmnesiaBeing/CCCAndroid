package edu.cuc.ccc;

import android.util.ArraySet;

import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Set;

import edu.cuc.ccc.utils.DeviceUtil.DeviceType;
import io.grpc.ManagedChannel;

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
    private DeviceType mType = DeviceType.Unknown;
    // 通过网络发现得到的IP地址与端口号
    private InetAddress ipAddrFromNetwork;
    private int portFromNetwork;
    // 是否已信任？
    private boolean trusted;
    // TODO:通过WIFI-P2P得到的IP地址与端口号（待调整，因为电脑作为GO，IP是已知的）
//    private IPPortAddr ipPortAddrFromWifiP2P;
    // TODO:通过二维码添加得到的物理信息（蓝牙和WIFI-P2P-GO）
    //
    // 通过二维码扫描会有个PIN码
    private String mPIN;
    // 设备支持的插件
    private Set<String> mSupportFeatures = new ArraySet<>();
    // 设备证书
    private Certificate mCertificate;
    // 设备密钥（私钥只有自己知道）
    private PublicKey mPublicKey;
    private PrivateKey mPrivateKey;
    // gRPC channel
    private ManagedChannel channel;

    //----------------------------------------------------------------------------------------------

    public int getProtocolVersion() {
        return mProtocolVersion;
    }

    public final void setProtocolVersion(int mProtocolVersion) {
        this.mProtocolVersion = mProtocolVersion;
    }

    public InetAddress getIpAddrFromNetwork() {
        return ipAddrFromNetwork;
    }

    public void setIPAddress(InetAddress addr) {
        ipAddrFromNetwork = addr;
    }

    public int getIpPortFromNetwork() {
        return portFromNetwork;
    }

    public void setPortFromNetwork(int port) {
        portFromNetwork = port;
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

    public String getPIN() {
        return mPIN;
    }

    public void setPIN(String PIN) {
        this.mPIN = PIN;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }
}
