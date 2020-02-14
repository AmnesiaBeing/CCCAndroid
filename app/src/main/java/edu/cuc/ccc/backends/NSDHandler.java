package edu.cuc.ccc.backends;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.cuc.ccc.packets.DeviceInfo;

public class NSDHandler {

    private final static String TAG = NSDHandler.class.getSimpleName();

    // 如何获取手机名称or自行设置？
    private final static String MyServiceName = "AAA";
    private final static String SERVICE_TYPE = "_ccc._tcp.";

    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;

    private NsdManager nsdManager;

    private List<NsdServiceInfo> services = new ArrayList<>();

    private Context mContext;

    // 设想中，这是目标连接设备的信息
    private DeviceInfo targetDevice;

    public NSDHandler(Context context) {
        mContext = context;
        nsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeDiscoveryListener() {
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

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // 从没进入这
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                // 这里需要做验证么？
                // 考虑到一个局域网内有多个mdns服务响应？
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                services.add(serviceInfo);
            }
        };
    }

    public void discoveryServices() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void onDestroy() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    public void setTargetDevice(DeviceInfo device) {
        targetDevice = device;
    }
}
