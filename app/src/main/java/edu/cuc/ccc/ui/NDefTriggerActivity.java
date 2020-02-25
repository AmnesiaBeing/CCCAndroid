package edu.cuc.ccc.ui;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import edu.cuc.ccc.R;
import edu.cuc.ccc.plugins.PluginFactory;
import io.grpc.ManagedChannel;

import static edu.cuc.ccc.backends.ConnectionManager.tryConnectDevice;

public class NDefTriggerActivity extends AppCompatActivity {

    private static final String TAG = NDefTriggerActivity.class.getSimpleName();

    public ManagedChannel channel;

    TextView tv_process_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();

        Intent intent = getIntent();
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // 这个数组长度通常只有1,因为只能有一个NdefMessage
            Parcelable[] raw = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            String uuid;
            if (raw != null) {
                // 一个NdefMessage中有许多的记录NdefRecord
                NdefMessage m = (NdefMessage) raw[0];
                // 按照写入的规则，只读取第1条记录中的数据（序号从0开始）
                NdefRecord[] records = m.getRecords();
                if (records.length == 3) {
                    uuid = new String(records[1].getPayload());
                    Log.i(TAG, "Get UUID from Ndef: " + uuid);
                    myConnectionTask.execute(uuid);
                } else {
                    // 如果写入的记录不符合要求
                    Toast.makeText(this, "传传传：读取NFC标签异常。", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        } else {
            finish();
        }
    }

    private void initViews() {
        setContentView(R.layout.activity_ndef_trigger);
        tv_process_status = findViewById(R.id.tv_process_status);
    }

    private MyConnectionTask myConnectionTask = new MyConnectionTask(this);

    private static class MyConnectionTask extends AsyncTask<String, Void, String> {

        WeakReference<NDefTriggerActivity> mainActivityWeakReference;

        MyConnectionTask(NDefTriggerActivity activity) {
            this.mainActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            NDefTriggerActivity activity = this.mainActivityWeakReference.get();

        }

        @Override
        protected String doInBackground(String... strings) {
            String uuid = strings[0];
            NDefTriggerActivity activity = this.mainActivityWeakReference.get();
            ManagedChannel channel;
            if (activity != null) {
                channel = activity.channel;
            } else {
                return null;
            }
            tryConnectDevice(uuid, null, channel);
            PluginFactory.initPluginInfo(uuid);

            // TODO:request what to do

            return uuid;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null) return;
            NDefTriggerActivity activity = this.mainActivityWeakReference.get();
            if (activity != null) {
                PluginFactory.pluginExecute(s);
            }
        }
    }

}
