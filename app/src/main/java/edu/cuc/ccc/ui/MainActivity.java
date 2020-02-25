package edu.cuc.ccc.ui;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.R;
import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.plugins.Plugin;
import edu.cuc.ccc.plugins.PluginFactory;
import edu.cuc.ccc.utils.DeviceUtil;
import io.grpc.ManagedChannel;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static edu.cuc.ccc.backends.ConnectionManager.tryConnectDevice;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static String TAG = MainActivity.class.getSimpleName();

    public ManagedChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_camera:
                // 点击右上角摄像头按钮后，可以添加新配对设备
                startScanQRCodeActivityForResult();
                break;
            case R.id.app_bar_info:
                // 点击右上角设置按钮后，打开设置页面
                startActivity(new Intent(this, DeviceInfoActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    TextView tv_tip_unpaired;
    LinearLayout ll_tip_pairing;
    LinearLayout ll_plugins_small_ui;

    private void initViews() {
        setContentView(R.layout.activity_main);

        tv_tip_unpaired = findViewById(R.id.tv_tip_unpaired);
        ll_tip_pairing = findViewById(R.id.ll_tip_paring);
        ll_plugins_small_ui = findViewById(R.id.plugins_small_ui);

        String uuid = DeviceManager.getInstance().getLastPairedDeviceUUID();

        if (uuid == null) {
            // 没有上一次记录，说明此时需要配对新设备
            setViewUnpaired();
        } else {
            // 有上一次的记录，直接读取内容即可，并且委托尝试连接上一次的设备
            myConnectionTask.execute(uuid);
        }
    }

    void setViewUnpaired() {
        ll_tip_pairing.setVisibility(View.GONE);
        ll_plugins_small_ui.setVisibility(View.GONE);
        tv_tip_unpaired.setVisibility(View.VISIBLE);
    }

    void setViewParing() {
        ll_tip_pairing.setVisibility(View.VISIBLE);
        ll_plugins_small_ui.setVisibility(View.GONE);
        tv_tip_unpaired.setVisibility(View.GONE);
    }

    void setViewPaired() {
        ll_tip_pairing.setVisibility(View.GONE);
        ll_plugins_small_ui.setVisibility(View.VISIBLE);
        tv_tip_unpaired.setVisibility(View.GONE);
    }

    private void initPluginViews(String uuid) {
        // 加载插件在主界面的选项界面
        ll_plugins_small_ui.removeAllViews();
        Map<String, Plugin> plugins = PluginFactory.getPlugins();
        for (Plugin plugin : plugins.values()) {
            if (plugin.hasViewInMainActivity()) {// && device.getSupportFeatures().contains(plugin.getPluginName())) {
                ll_plugins_small_ui.addView(plugin.getViewInMainActivity(this));
            }
        }
    }

    // 通过扫二维码方式配对，返回的结果交给DeviceManager解析
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 0 || resultCode != 0) return;
        if (data == null) return;
        String result = data.getStringExtra("RESULT");
        if (result == null) return;
        Log.i(TAG, result);
        // TODO:Base64编码
        Device targetDevice = DeviceUtil.parseJSONString(result);
        if (targetDevice == null) {
            Toast.makeText(this, R.string.scan_QRCode_err, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.scan_QRCode_ok, Toast.LENGTH_LONG).show();
            myConnectionTask.cancel(true);
            myConnectionTask.execute(targetDevice.getUUID(), targetDevice.getPIN());
        }
    }

    private static final int RC_CAMERA_PERM = 123;

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private MyConnectionTask myConnectionTask = new MyConnectionTask(this);

    private static class MyConnectionTask extends AsyncTask<String, Void, String> {

        WeakReference<MainActivity> mainActivityWeakReference;

        MyConnectionTask(MainActivity activity) {
            this.mainActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            MainActivity activity = this.mainActivityWeakReference.get();
            if (activity != null) {
                activity.setViewParing();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String uuid = strings[0];
            String pin = null;
            if (strings.length >= 2) {
                pin = strings[1];
            }
            MainActivity activity = this.mainActivityWeakReference.get();
            ManagedChannel channel;
            if (activity != null) {
                channel = activity.channel;
            } else {
                return null;
            }
            tryConnectDevice(uuid, pin, channel);
            PluginFactory.initPluginInfo(uuid);
            return uuid;
        }

        @Override
        protected void onPostExecute(String uuid) {
            if (uuid == null) return;
            MainActivity activity = this.mainActivityWeakReference.get();
            if (activity != null) {
                activity.setViewPaired();
                activity.initPluginViews(uuid);
            }
        }
    }
}
