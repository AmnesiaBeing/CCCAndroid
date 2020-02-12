package edu.cuc.readnfctag2share.helpers;

import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkHelper {
    private final static String TAG = NetworkHelper.class.getSimpleName();

    private Context mContext;
    private ConnectivityManager connectivityManager;


    public NetworkHelper(Context context) {
        mContext = context;
        context.getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    public boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo.getNetworkId() == -1) {
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

}
