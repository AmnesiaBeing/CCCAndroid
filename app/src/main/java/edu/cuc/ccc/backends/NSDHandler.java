package edu.cuc.ccc.backends;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import edu.cuc.ccc.Device;

import static edu.cuc.ccc.DeviceUtil.*;

// 负责管理网络发现的的类
class NSDHandler {

    private final static String TAG = NSDHandler.class.getSimpleName();

    // 如何获取手机名称or自行设置？
    private final static String MyServiceName = "AAA";
    private final static String SERVICE_TYPE = "_ccc._tcp.";

    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;

    private NsdManager nsdManager;

    NSDHandler(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    void onCreate() {
        initializeDiscoveryListener();
        initializeResolveListener();
        discoveryServices();
    }

    void onDestroy() {
        stopDiscoveryServices();
    }

    private void initializeDiscoveryListener() {
        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // 本人测试，从来没进过这个分支
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(MyServiceName)) {
                    // 防止连接到自己同名字的替身
                    Log.d(TAG, "Same machine: " + MyServiceName);
                } else {
                    // 其余情况：类型相同，名字不同，那应该是
                    Log.d(TAG, "resolve:  " + service.getServiceName());
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // 从没进入这
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(final NsdServiceInfo serviceInfo) {
                // 这里需要做验证么？
                // 考虑到一个局域网内有多个mdns服务响应？
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                Device device = new Device();
                device.setName(serviceInfo.getServiceName());
                device.addIPPortAddress(
                        new IPPortAddr(serviceInfo.getHost(), serviceInfo.getPort())
                );
                byte[] rawDT = serviceInfo.getAttributes().get("DT");
                byte[] rawUUID = serviceInfo.getAttributes().get("UUID");
                if (rawUUID == null) return;
                device.setType((rawDT != null) ?
                        DeviceType.valueOfEX(new String(rawDT)) : DeviceType.Unknown);
                device.setUUID(new String(rawUUID));
                DeviceManager.getInstance().addNewFoundDevice(device);
            }
        };
    }

    private void discoveryServices() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void stopDiscoveryServices() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

}
