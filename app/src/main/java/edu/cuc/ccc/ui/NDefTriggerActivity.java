package edu.cuc.ccc.ui;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.R;
import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.plugins.PluginBase;
import edu.cuc.ccc.plugins.PluginFactory;

public class NDefTriggerActivity extends AppCompatActivity implements PluginBase.PluginProcessCallback {

    private static final String TAG = NDefTriggerActivity.class.getSimpleName();

    public static final int PLUGIN_TASK_COMPLETE = 1;
    public static final int PLUGIN_TASK_MESSAGE = 2;

    TextView tv_process_status;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case PLUGIN_TASK_COMPLETE:
                    tv_process_status.append((String) msg.obj);
                    break;
                case PLUGIN_TASK_MESSAGE:
                    tv_process_status.append((String) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        Intent intent = getIntent();
        String action = intent.getAction();
        Log.i(TAG, "onCreate: " + intent);

        String pluginName = intent.getStringExtra("plugin");

        if (action == null) {
            finish();
        } else if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            // 这个数组长度通常只有1,因为只能有一个NdefMessage
            Parcelable[] raw = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            String uuid;
            if (raw != null) {
                // 一个NdefMessage中有许多的记录NdefRecord
                NdefMessage m = (NdefMessage) raw[0];
                // 按照写入的规则，只读取第1条记录中的数据（序号从0开始）
                NdefRecord[] records = m.getRecords();
                if (records.length >= 1) {
                    uuid = new String(records[1].getPayload());
                    Log.i(TAG, "onCreate: " + uuid);
                    Device d = DeviceManager.getInstance().searchDevice(uuid);
                    if (d == null) {
                        // 如果为空，说明从来没配对过，这个客户端不可信任，需要弹出提示框，说明这项操作不被允许
                        Toast.makeText(this, "读取NFC标签异常，客户端不被信任。", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        DeviceManager.getInstance().addNewFoundDevice(d);
                        DeviceManager.getInstance().setPairingDevice(d);
                    }
                }
            }
            // Whatever，试一试吧
            PluginFactory.doPluginProcess("pair", this);
        } else {
            if (pluginName != null && !pluginName.isEmpty())
                PluginFactory.doPluginProcess(pluginName, this);
        }
    }

    private void initViews() {
        setContentView(R.layout.activity_ndef_trigger);
        tv_process_status = findViewById(R.id.tv_process_status);
    }

    @Override
    public void PluginProcessMessage(PluginBase plugin, String str) {
        Message msg = new Message();
        msg.what = PLUGIN_TASK_MESSAGE;
        msg.obj = str;
        handler.sendMessage(msg);
    }

    @Override
    public void PluginProcessComplete(PluginBase plugin, String str) {
        Message msg = new Message();
        msg.what = PLUGIN_TASK_COMPLETE;
        msg.obj = str;
        handler.sendMessage(msg);
    }

}
