package edu.cuc.ccc;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.UUID;

import static edu.cuc.ccc.MyApplication.appContext;

public class DeviceUtil {

    // 表示设备状态的枚举类型
    public static enum DeviceStatus {
        // 初始状态
        Unknown,
        // 已配对
        Paired,
        // 正在配对
        Pairing
    }

    public enum AddrType {
        // 无知者无畏
        Unknown,
        LAN,
        WLAN,
        BT,
        // 低功耗蓝牙设备带宽太小，就暂时不考虑了
//        BLE
    }

    public static class IPPortAddr {
        private AddrType type;
        private InetAddress addr;
        private int port;

        public IPPortAddr(AddrType type, InetAddress addr, int port) {
            this.type = type;
            this.addr = addr;
            this.port = port;
        }

        public IPPortAddr(InetAddress addr, int port) {
            this.type = AddrType.Unknown;
            this.addr = addr;
            this.port = port;
        }

        public InetAddress getAddr() {
            return addr;
        }

        public int getPort() {
            return port;
        }

        public AddrType getType() {
            return type;
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

    //----------------------------------------------------------------------------------------------

    // 转换成json数据
    public static String toJSONStr(Device device) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(appContext.getResources().getString(R.string.KEY_VERSION), device.getProtocolVersion());
            jsonObject.put(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE), device.getDeviceType());
            jsonObject.put(appContext.getResources().getString(R.string.KEY_DEVICE_NAME), device.getDeviceName());
            {
                JSONArray jsonArray = new JSONArray();
                for (final IPPortAddr i : device.getDeviceIPPortAddress()) {
                    jsonArray.put(new JSONObject() {{
                        // TODO:
                        put(appContext.getResources().getString(R.string.KEY_DEVICE_IP), i.addr);
                        put(appContext.getResources().getString(R.string.KEY_DEVICE_PORT), i.port);
                    }});
                }
                jsonObject.put(appContext.getResources().getString(R.string.KEY_DEVICE_IP_PORT), jsonArray);
            }
            {
                JSONArray jsonArray = new JSONArray();
                for (String i : device.getSupportFeatures()) {
                    jsonArray.put(i);
                }
                jsonObject.put(appContext.getResources().getString(R.string.KEY_DEVICE_FEATURES), jsonArray);
            }
        } catch (Exception e) {
            return "";
        }
        return jsonObject.toString();
    }

    // 解析传入的json数据，如解析不成功返回null
    public static Device parseJSONStr(final String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            int ver = jsonObject.getInt(appContext.getResources().getString(R.string.KEY_VERSION));
            DeviceType devType;
            try {
                devType = DeviceType.valueOf(jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE)));
            } catch (IllegalArgumentException e) {
                devType = DeviceType.Unknown;
            }
            String uuid = jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_UUID));
            String devName = jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_NAME));
            String Certificate = jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_CERTIFICATE));
            String PublicKey = jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_PUBLIC_KEY));
            String PrivateKey = jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_PRIVATE_KEY));
            Device device = new Device(uuid, devName, devType, Certificate, PublicKey, PrivateKey);
            device.setProtocolVersion(ver);
            return device;
        } catch (Exception e) {
            return null;
        }
    }

    //----------------------------------------------------------------------------------------------

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "MY_UUID";

    public synchronized static String getMyId() {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = MySharedPreferences.getApplicationSharedPreferences();
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                sharedPrefs.edit().putString(PREF_UNIQUE_ID, uniqueID).apply();
            }
        }
        return uniqueID;
    }

    private static String name = null;
    private static final String PREF_NAME = "MY_NAME";

    public synchronized static String getMyName() {
        if (name == null) {
            SharedPreferences sharedPrefs = MySharedPreferences.getApplicationSharedPreferences();
            name = sharedPrefs.getString(PREF_NAME, null);
            if (name == null) {
                name = getMyId();
                sharedPrefs.edit().putString(PREF_NAME, name).apply();
            }
        }
        return uniqueID;
    }

    public synchronized static void setMyName(String name) {
        if (name != null) {
            SharedPreferences sharedPrefs = MySharedPreferences.getApplicationSharedPreferences();
            sharedPrefs.edit().putString(PREF_NAME, name).apply();
        }
    }
}
