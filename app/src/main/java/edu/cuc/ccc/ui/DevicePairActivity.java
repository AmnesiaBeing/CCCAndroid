package edu.cuc.ccc.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import edu.cuc.ccc.R;
import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.helpers.BackendServiceHelper;
import edu.cuc.ccc.helpers.NFCHelper;
import edu.cuc.ccc.Device;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class DevicePairActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, NFCHelper.NFCTagEventListener {
    private static String TAG = DevicePairActivity.class.getSimpleName();

    private NFCHelper nfcHelper;
    private static final int RC_CAMERA_PERM = 123;

    private BackendServiceHelper backendServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backendServiceHelper = new BackendServiceHelper(this);
        nfcHelper = new NFCHelper(this);
        nfcHelper.setNFCTagWriteListener(this);

        initViews();
    }

    private void initViews() {
        setContentView(R.layout.activity_device_pair);
//        findViewById(R.id.btn_write_tag).setOnClickListener(this);

        final SwipeRefreshLayout srl = findViewById(R.id.srl_devices);

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initDeviceView();
                srl.setRefreshing(false);
            }
        });

        ((ListView) findViewById(R.id.lv_devices)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BackendService.getInstance().getDeviceManager().requestPairDevice((Device) view.getTag());
            }
        });

        TextView tv_nfc_status = findViewById(R.id.tv_nfc_status);
        if (!nfcHelper.isSupportNFC()) {
            tv_nfc_status.setText(R.string.tip_nfc_nosupport);
        } else if (!nfcHelper.isEnableNFC()) {
            tv_nfc_status.setText(R.string.tip_nfc_disable);
        }

        initDeviceView();
    }

    private void initDeviceView() {
        List<Device> devices = BackendService.getInstance().getDeviceManager().getDevices();
        ((ListView) findViewById(R.id.lv_devices)).setAdapter(new DeviceAdapter(this, devices));
    }

    class DeviceAdapter extends BaseAdapter {
        private Context mContext;
        private List<Device> devices;

        DeviceAdapter(Context context, List<Device> devices) {
            this.mContext = context;
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Device device = (Device) getItem(position);
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_device_info, null);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.item_device_name)).setText(device.getDeviceName());
            ((ImageView) view.findViewById(R.id.item_device_type)).
                    setImageDrawable(mContext.getDrawable(device.getDeviceType().getDrawableId()));
            view.setTag(device);
            return view;
        }

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
        menuInflater.inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_bar_add) {
            startScanQRCodeActivityForResult();
        }
        return super.onOptionsItemSelected(item);
    }

    // 通过扫二维码方式配对将，认为直接能够配对上:)
    // 扫描后返回的结果给DeviceInfo解析一下
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 0 || resultCode != 0) return;
        if (data == null) return;
        String result = data.getStringExtra("RESULT");
        if (result == null) return;
        Log.i(TAG, result);
        Device targetDevice = Device.parseJSONStr(result);
        if (targetDevice == null) {
            Toast.makeText(this, R.string.scan_QRCode_err, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.scan_QRCode_ok, Toast.LENGTH_LONG).show();
//            findViewById(R.id.v_step2).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tv_scan_result)).setText(String.format("%s%s", getString(R.string.dev_name), targetDevice.getDeviceName()));
//            Intent intent = new Intent();
//            intent.putExtra("command", BackendService.Command.CMD_CONNECT_DEVICE);
//            intent.putExtra("extra", targetDevice);
            // FIXME:此处存在设计上的逻辑问题
            BackendService.getInstance().getDeviceManager().putNewFoundDevice(targetDevice);
//            nfcHelper.refreshWriteContent();
            nfcHelper.setWriteContent(targetDevice);
//            findViewById(R.id.btn_write_tag).setEnabled(true);
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
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
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
        Toast.makeText(this, R.string.tag_write_ok, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNFCTagWriteError(int strId) {
        Toast.makeText(this, strId, Toast.LENGTH_SHORT).show();
    }
}
