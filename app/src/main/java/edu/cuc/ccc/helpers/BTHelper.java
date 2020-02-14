package edu.cuc.ccc.helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

// 蓝牙权限请求、配对、连接与开启SPP协议的帮助类
/*
    Android经典蓝牙的开发步骤如下:
        1.扫描其他蓝牙设备
        2.查询本地蓝牙适配器的配对蓝牙设备
        3.建立 RFCOMM 通道 (SPP协议)
        4.通过服务发现连接到其他设备
        5.与其他设备进行双向数据传输
        6.管理多个连接
 */
public class BTHelper {
    private static String TAG = BTHelper.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    public BTHelper(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
    }

    public void onCreate() {
        if (mBluetoothAdapter == null) {
            return;
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver, filter);
        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
        }
    }

    public void onDestroy() {
        if (mBluetoothAdapter == null) {
            return;
        }
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Log.i(TAG, "onReceive: " + state);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:

                        break;
                }
            }
        }
    };

    public void connect(BluetoothDevice dev) {
        
    }
}
