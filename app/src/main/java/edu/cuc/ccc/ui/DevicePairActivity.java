package edu.cuc.ccc.ui;

import android.Manifest;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.cuc.ccc.R;
import edu.cuc.ccc.helpers.BackendServiceHelper;
import edu.cuc.ccc.helpers.NFCHelper;
import edu.cuc.ccc.packets.DeviceInfo;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class DevicePairActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks, NFCHelper.NFCTagEventListener {
    private static String TAG = DevicePairActivity.class.getSimpleName();
    private NFCHelper nfcHelper;
    private static final int RC_CAMERA_PERM = 123;

    private BackendServiceHelper backendServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtag);
//        findViewById(R.id.btn_begin_scan_QRCode).setOnClickListener(this);
//        findViewById(R.id.btn_begin_add_tag).setOnClickListener(this);
        findViewById(R.id.btn_write_tag).setOnClickListener(this);
        backendServiceHelper = new BackendServiceHelper(this);
        nfcHelper = new NFCHelper(this);
        nfcHelper.setNFCTagWriteListener(this);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        backendServiceHelper.onStart();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        backendServiceHelper.onStop();
        super.onStop();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        if (intent != null) {
            if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                nfcHelper.resolveNFCIntent(intent);
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
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_begin_add_tag:
//                findViewById(R.id.btn_begin_add_tag).setEnabled(false);
//                findViewById(R.id.v_step1).setVisibility(View.VISIBLE);
//            case R.id.btn_begin_scan_QRCode:
//                startScanQRCodeActivityForResult();
//                break;
//            case R.id.btn_write_tag:
//                finish();
//                break;
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_add:
                startScanQRCodeActivityForResult();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 扫描后返回的结果给DeviceInfo解析一下
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 0 || resultCode != 0) return;
        if (data == null) return;
        String result = data.getStringExtra("RESULT");
        if (result == null) return;
        Log.i(TAG, result);
        DeviceInfo deviceInfo = DeviceInfo.parseJSONStr(result);
        if (deviceInfo == null) {
            Toast.makeText(this, R.string.scan_QRCode_err, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.scan_QRCode_ok, Toast.LENGTH_LONG).show();
            findViewById(R.id.v_step2).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tv_scan_result)).setText(getString(R.string.dev_name) + deviceInfo.getDeviceName());
            Intent intent = new Intent();
//            intent.putExtra("command", BackendService.Command.CMD_CONNECT_DEVICE);
            intent.putExtra("extra", deviceInfo);
            // NFCHelper prepare write tag
        }
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    public void startScanQRCodeActivityForResult() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            startActivityForResult(new Intent(this, ScanQRCodeActivity.class), 0);
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera),
                    RC_CAMERA_PERM,
                    Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

//    @Override
//    public void BackendServiceCallback() {
//
//    }

    @Override
    public void onNFCTagWriteCompleted() {
        ((TextView) findViewById(R.id.tv_write_result)).setText(R.string.tag_write_ok);
    }

    @Override
    public void onNFCTagWriteError(int strId) {
        ((TextView) findViewById(R.id.tv_write_result)).setText(strId);
    }
}
