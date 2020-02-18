package edu.cuc.ccc.ui;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.cuc.ccc.R;
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
            // TODO: 读取存储的HASH信息
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
