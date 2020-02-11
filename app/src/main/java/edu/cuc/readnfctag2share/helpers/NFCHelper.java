package edu.cuc.readnfctag2share.helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;

public class NFCHelper {
    private static String TAG = "NFCHelper";

    private Activity mActivity;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    private NFCTagWriteListener mListener;

    public NFCHelper(Activity activity) {
        this.mActivity = activity;
        mAdapter = NfcAdapter.getDefaultAdapter(mActivity);
        mPendingIntent = PendingIntent.getActivity(mActivity, 0,
                new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        ndef.addCategory(Intent.CATEGORY_DEFAULT);
        mFilters = new IntentFilter[]{ndef};
        mTechLists = new String[][]{};
    }

    public void setupForegroundDispatch() {
        if (mAdapter == null) return;
        mAdapter.enableForegroundDispatch(mActivity, mPendingIntent, mFilters, mTechLists);
        Log.i("Foreground dispatch", "setupForegroundDispatch");
    }

    public void undoForegroundDispatch() {
        if (mAdapter == null) return;
        mAdapter.disableForegroundDispatch(mActivity);
        Log.i("Foreground dispatch", "undoForegroundDispatch");
    }

    public void resolveNFCIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
    }

    public boolean isSupportNFC() {
        return (mAdapter != null);
    }

    public boolean isEnableNFC() {
        return mAdapter.isEnabled();
    }

    public void setNFCTagWriteListener(NFCTagWriteListener listener) {
        mListener = listener;
    }

    public interface NFCTagWriteListener {
        void onWriteCompleted();
//        void onWriteError();
    }
}
