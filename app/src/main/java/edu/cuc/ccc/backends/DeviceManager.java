package edu.cuc.ccc.backends;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.DeviceUtil;
import edu.cuc.ccc.MySharedPreferences;
import edu.cuc.ccc.R;

import static edu.cuc.ccc.DeviceUtil.DeviceType;
import static edu.cuc.ccc.MyApplication.appContext;

// 负责管理发现的设备（包括自己）
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

    // UUID,Device
    private Map<String, Device> knownDevices = new LinkedHashMap<>();

    private static final String KEY_LAST_PAIRED_UUID = "LPD";

    // 程序启动时会调用这个函数，从配置中读取上一次连接的配对信息
    public Device getLastPairedDevice() {
        SharedPreferences applicationSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
        String uuid = applicationSharedPreferences.getString(KEY_LAST_PAIRED_UUID, null);
        if (uuid == null) return null;
        SharedPreferences deviceSharedPreferences = MySharedPreferences.getSharedPreferences(uuid);
        String name = deviceSharedPreferences.getString(
                appContext.getResources().getString(R.string.KEY_DEVICE_NAME), "");
        DeviceType type = DeviceType.valueOfEX(deviceSharedPreferences.getString(
                appContext.getString(R.string.KEY_DEVICE_TYPE), null));
        return new Device(uuid, name, type);
    }

    public void setLastPairedDevice(Device device) {
        SharedPreferences applicationSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
        String uuid = device.getUUID();
        applicationSharedPreferences.edit()
                .putString(KEY_LAST_PAIRED_UUID, uuid)
                .apply();
    }

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

    public Device getPairedDevice() {
        for (Device d : knownDevices.values()) {
            if (d.isPaired()) return d;
        }
        return null;
    }

    // 通过网络发现、二维码扫描得到一个新设备
    public void addNewFoundDevice(Device device) {
        knownDevices.put(device.getUUID(), device);
    }

    public List<Device> getDevices() {
        return new ArrayList<>(knownDevices.values());
    }

    public Device searchDevice(String uuid) {
        return knownDevices.get(uuid);
    }

    public Device getMyDevice() {
        return myDevice;
    }

    public Device getPairingDevice() {
        for (Device d : knownDevices.values()) {
            if (d.isParing()) return d;
        }
        return null;
    }

    public void setPairingDevice(Device d) {
        Device pd = getPairedDevice();
        if (pd != null) pd.setStatus(DeviceUtil.DeviceStatus.Unknown);
        d.setStatus(DeviceUtil.DeviceStatus.Pairing);
    }

    public void setPairingDevice2PairedDevice() {
        Device pairingDevice = getPairingDevice();
        pairingDevice.setStatus(DeviceUtil.DeviceStatus.Paired);
        setLastPairedDevice(pairingDevice);
    }

    public void setPairingDevice2UnknownDevice() {
        Device pairingDevice = getPairingDevice();
        pairingDevice.setStatus(DeviceUtil.DeviceStatus.Unknown);
    }
}
