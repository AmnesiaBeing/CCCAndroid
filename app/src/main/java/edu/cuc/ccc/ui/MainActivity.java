package edu.cuc.ccc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.R;
import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.plugins.PluginBase;
import edu.cuc.ccc.plugins.PluginFactory;


public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DeviceManager.getInstance().getLastPairedDevice() == null) {
            Intent intent = new Intent(this, DevicePairActivity.class);
            startActivity(intent);
        }
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.plus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_bar_add) {
            startActivity(new Intent(this, DevicePairActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        setContentView(R.layout.activity_main);

        TextView tv_tip_unpaired = findViewById(R.id.tv_tip_unpaired);

        Device device = DeviceManager.getInstance().getPairedDevice();
        if (device != null) {
            tv_tip_unpaired.setVisibility(View.GONE);
            initPluginViews(device);
        } else {
            tv_tip_unpaired.setVisibility(View.VISIBLE);
        }
    }

    private void initPluginViews(Device device) {
        // 加载插件在主界面的选项界面
        LinearLayout ll = findViewById(R.id.plugins_small_ui);
        Map<String, PluginBase> plugins = PluginFactory.getPlugins();
        for (PluginBase plugin : plugins.values()) {
            if (plugin.hasViewInMainActivity() && device.getSupportFeatures().contains(plugin.getPluginName())) {
                ll.addView(plugin.getViewInMainActivity(this));
            }
        }
    }

//    @Override
//    public void onCheckedChanged(RadioGroup group, int checkedId) {
//        MySharedPreferences.getApplicationSharedPreferences().edit().putInt("TransMethod", TransMethodID2Index(checkedId)).apply();
//    }


//        RadioGroup rgTransMethod = findViewById(R.id.rg_trans_method);
//        rgTransMethod.setOnCheckedChangeListener(this);
    // 好长
//        ((RadioButton) findViewById(
//                TransMethodIndex2ID(
//                        MySharedPreferences.getApplicationSharedPreferences().getInt("TransMethod", 0))))
//                .setChecked(true);
//    }

//    private final int[] map = new int[]{
//            R.id.rb_trans_method_auto, R.id.rb_trans_method_wlan,
//            R.id.rb_trans_method_p2p, R.id.rb_trans_method_bt
//    };
//
//    // 不会改成map啊
//    private int TransMethodID2Index(int id) {
//        int value = 0;
//        switch (id) {
//            case R.id.rb_trans_method_auto:
//                value = 0;
//                break;
//            case R.id.rb_trans_method_wlan:
//                value = 1;
//                break;
//            case R.id.rb_trans_method_p2p:
//                value = 2;
//                break;
//            case R.id.rb_trans_method_bt:
//                value = 3;
//                break;
//        }
//        return value;
//    }
//
//    private int TransMethodIndex2ID(int value) {
//        if (value > map.length || value < 0) value = 0;
//        return map[value];
//    }
//
//    @Override
//    public void onClick(View v) {
//        new RPCHandler.GrpcTask(new RPCHandler.ShareClipBoardRunnable(), channel, backendServiceHelper.mService).execute();
//    }
}
