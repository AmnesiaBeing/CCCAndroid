package edu.cuc.ccc.backends;

import android.content.SharedPreferences;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.MySharedPreferences;
import edu.cuc.ccc.R;

import static edu.cuc.ccc.utils.DeviceUtil.DeviceType;
import static edu.cuc.ccc.utils.DeviceUtil.getX509CertificateFromBase64EncodedString;
import static edu.cuc.ccc.MyApplication.appContext;

// 负责管理设备信息（包括自己）
// 20200221重新思考了一下，这个软件只为1对1的连接服务，完全不需要考虑多设备的情况
public class DeviceManager {

    private static final String TAG = DeviceManager.class.getSimpleName();

    private static DeviceManager instance;

    public static DeviceManager getInstance() {
        return instance;
    }

    public DeviceManager() {
        instance = this;
    }

    //----------------------------------------------------------------------------------------------

    // 本设备的信息
    private Device myDevice = new Device();

    // TODO:优化连接方式的存储方法
    // 这个MAP只记录通过网络发现的设备信息，所以在这里头的都有连接方式
    private Map<String, Device> knownDevices = new LinkedHashMap<>();

    private Device lastUsedDevice;

    private static final String KEY_LAST_PAIRED_UUID = "LPD";

    // 从配置中读取上一次连接的配对信息
    public String getLastPairedDeviceUUID() {
        SharedPreferences applicationSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
        return applicationSharedPreferences.getString(KEY_LAST_PAIRED_UUID, null);
    }

    // 设置上一次连接的设备UUID
    public void setLastPairedDeviceUUID(String uuid) {
        SharedPreferences applicationSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
        applicationSharedPreferences.edit()
                .putString(KEY_LAST_PAIRED_UUID, uuid)
                .apply();
    }

    // 从配置中读取设备信息
    private Device restoreDevice(String uuid) {
        SharedPreferences deviceSharedPreferences = MySharedPreferences.getSharedPreferences(uuid);

        String name = deviceSharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_NAME), null);
        DeviceType type = DeviceType.valueOfEX(deviceSharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE), null));
        Certificate cert = null;
//        PublicKey pubk = null;
//        PrivateKey prik = null;
        try {
            cert = getX509CertificateFromBase64EncodedString(deviceSharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_CERTIFICATE), null));
//            pubk = getPublicKeyFromBase64EncodedString(deviceSharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_PUBLIC_KEY), null));
//            prik = getPrivateKeyFromBase64EncodedString(deviceSharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_PRIVATE_KEY), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO:cert
        return new Device(uuid, name, type);
    }

    // 将设备信息存储下来
    public void storeDevice(Device device) {
        SharedPreferences deviceSharedPreferences = MySharedPreferences.getSharedPreferences(device.getUUID());
        deviceSharedPreferences.edit()
                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_NAME), device.getName())
                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE), device.getType().name())
//                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_CERTIFICATE), getBase64EncodedStringFromCertificate(device.getCertificate()))
//                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_PUBLIC_KEY), getBase64EncodedStringFromPublicKey(device.getPublicKey()))
//                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_PRIVATE_KEY), getBase64EncodedStringFromPrivateKey(device.getPrivateKey()))
                .apply();
    }

    // 通过网络发现得到一个新设备，直接添加到列表中
    void addNewFoundDeviceFromNSD(Device device) {
        knownDevices.put(device.getUUID(), device);
    }

    public List<Device> getDevices() {
        return new ArrayList<>(knownDevices.values());
    }

    // 通过UUID获取设备信息
    public Device searchDevice(String uuid) {
        return knownDevices.get(uuid);
    }

    public Device getMyDevice() {
        return myDevice;
    }

}
