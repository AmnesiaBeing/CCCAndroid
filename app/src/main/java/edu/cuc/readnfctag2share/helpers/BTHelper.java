package edu.cuc.readnfctag2share.helpers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

public class BTHelper {
    private static String TAG = BTHelper.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;

    public BTHelper(Activity activity) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }



}
