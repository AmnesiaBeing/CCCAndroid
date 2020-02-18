package edu.cuc.ccc.backends;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.DeviceUtil;
import edu.cuc.ccc.MySharedPreferences;
import edu.cuc.ccc.R;

import static edu.cuc.ccc.MyApplication.appContext;

// 负责管理发现的设备（包括自己）
public class DeviceManager {

    // 本设备的信息
    private Device myDevice;

    // UUID,Device
    private Map<String, Device> knownDevices = new LinkedHashMap<>();

    private static final String KEY_LAST_PAIRED_UUID = "LPD";

    // 程序启动时会调用这个函数，从配置中读取上一次连接的配对信息
    public Device getLastPairedDevice() {
        SharedPreferences globalSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
        String uuid = globalSharedPreferences.getString(KEY_LAST_PAIRED_UUID, null);
        if (uuid == null) return null;
        SharedPreferences sharedPreferences = MySharedPreferences.getSharedPreferences(uuid);
        String name = sharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_NAME), "");
        String cert = sharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_CERTIFICATE), "");
        String pubk = sharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_PUBLIC_KEY), "");
        String prik = sharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_PRIVATE_KEY), "");
        String type = sharedPreferences.getString(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE), "");
        return new Device(uuid, name, DeviceUtil.DeviceType.valueOf(type), cert, pubk, prik);
    }

    public void setLastPairedDevice(Device device) {
        SharedPreferences globalSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
        String uuid = device.getDeviceUUID();
        globalSharedPreferences.edit()
                .putString(KEY_LAST_PAIRED_UUID, uuid)
                .apply();
        SharedPreferences sharedPreferences = MySharedPreferences.getSharedPreferences(uuid);
        sharedPreferences.edit()
                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_NAME), device.getDeviceName())
                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE), device.getDeviceType().name())
                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_CERTIFICATE), device.getCertificate())
                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_PUBLIC_KEY), device.getPublicKey())
                .putString(appContext.getResources().getString(R.string.KEY_DEVICE_PRIVATE_KEY), device.getPrivateKey())
                .apply();
    }

    public Device getPairedDevice() {
        for (Device d : knownDevices.values()) {
            if (d.isPaired()) return d;
        }
        return null;
    }

    // 通过二维码扫描得到一个新设备
    public void putNewFoundDevice(Device device) {
        setPairingDevice(device);
        knownDevices.put(device.getDeviceUUID(), device);
    }

    public List<Device> getDevices() {
        return new ArrayList<>(knownDevices.values());
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

    public void setPairingDevice2PairedDevice(Device d) {
        d.setStatus(DeviceUtil.DeviceStatus.Paired);
        setLastPairedDevice(d);
    }
}
