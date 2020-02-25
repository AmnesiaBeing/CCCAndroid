package edu.cuc.ccc.ui;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.cuc.ccc.R;
import edu.cuc.ccc.helpers.NFCHelper;

// 这个Acitivity用于展示信息，设置，写标签
public class DeviceInfoActivity extends AppCompatActivity implements NFCHelper.NFCTagEventListener {
    private static String TAG = DeviceInfoActivity.class.getSimpleName();

    NFCHelper nfcHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcHelper = new NFCHelper(this);
        nfcHelper.setNFCTagWriteListener(this);

        initViews();
    }

    private void initViews() {
        setContentView(R.layout.activity_device_pair);

        // TODO:initView
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                    nfcHelper.resolveNFCIntent(intent);
                }
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        nfcHelper.setupForegroundDispatch();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        nfcHelper.undoForegroundDispatch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO:判断NFC功能是否正常
        if (item.getItemId() == R.id.app_bar_write_tag) {
            // FIXME:
            nfcHelper.setWriteContent("");
            nfcHelper.setEnable(true);
            nfcHelper.setupForegroundDispatch();
            Toast.makeText(this,"现在可以写标签，请将手机靠近标签。",Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNFCTagWriteCompleted() {
        Toast.makeText(this, R.string.tag_write_ok, Toast.LENGTH_LONG).show();
        nfcHelper.setEnable(false);
        nfcHelper.undoForegroundDispatch();
    }

    @Override
    public void onNFCTagWriteError(int strId) {
        Toast.makeText(this, strId, Toast.LENGTH_SHORT).show();
    }

}
