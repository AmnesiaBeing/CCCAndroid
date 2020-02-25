package edu.cuc.ccc.ui;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.R;
import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.helpers.NFCHelper;
import edu.cuc.ccc.plugins.PluginBase;
import edu.cuc.ccc.plugins.PluginFactory;
import edu.cuc.ccc.plugins.connection.ConnectionPlugin;

public class DeviceInfoActivityBak extends AppCompatActivity implements NFCHelper.NFCTagEventListener, ConnectionPlugin.PairRequestCallback {
    private static String TAG = DeviceInfoActivityBak.class.getSimpleName();

    private NFCHelper nfcHelper;

    public static final int PLUGIN_TASK_COMPLETE = 1;

    ListView lv_devices;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == PLUGIN_TASK_COMPLETE) {
                adapter.notifyDataSetChanged();
                Toast.makeText(DeviceInfoActivityBak.this, (String) msg.obj, Toast.LENGTH_LONG).show();
            } else {
                super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcHelper = new NFCHelper(this);
        nfcHelper.setNFCTagWriteListener(this);

        initViews();
    }

    private void initViews() {
        setContentView(R.layout.activity_device_pair);

        final SwipeRefreshLayout srl = findViewById(R.id.srl_devices);

        adapter = new DeviceGroupAdapter(this);

        srl.setOnRefreshListener(() -> {
            adapter.notifyDataSetChanged();
            srl.setRefreshing(false);
        });

        lv_devices = findViewById(R.id.lv_devices);

        lv_devices.setOnItemClickListener((parent, view, position, id) -> {
            DeviceManager.getInstance().setPairingDevice((Device) adapter.getItem(position));
            PluginFactory.doPluginProcess("pair", DeviceInfoActivityBak.this);
            adapter.notifyDataSetChanged();
        });

        TextView tv_nfc_status = findViewById(R.id.tv_nfc_status);
        if (!nfcHelper.isSupportNFC()) {
            tv_nfc_status.setText(R.string.tip_nfc_nosupport);
        } else if (!nfcHelper.isEnableNFC()) {
            tv_nfc_status.setText(R.string.tip_nfc_disable);
        }

        lv_devices.setAdapter(adapter);
    }

    DeviceGroupAdapter adapter;

    @Override
    public void onPairRequestError() {

    }

    @Override
    public void onPairRequestComplete() {

    }

    @Override
    public void PluginProcessMessage(PluginBase plugin, String str) {

    }

    @Override
    public void PluginProcessComplete(PluginBase plugin, String str) {

    }

    class DeviceGroupAdapter extends BaseAdapter {
        private List<Device> devices;
        private Context context;

        DeviceGroupAdapter(Context context) {
            this.context = context;
            devices = new ArrayList<>();
            notifyDataSetChanged();
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
        public void notifyDataSetChanged() {
            devices.clear();
            List<Device> tmp = DeviceManager.getInstance().getDevices();
            Device pD = DeviceManager.getInstance().getPairedDevice();
            if (pD != null) {
                devices.add(pD);
                devices.add(null);
            }
            pD = DeviceManager.getInstance().getPairingDevice();
            if (pD != null) {
                devices.add(pD);
                devices.add(null);
            }
            for (Device item : tmp) {
                if (!(item.isParing() || item.isPaired())) {
                    devices.add(item);
                }
            }
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Device device = (Device) getItem(position);
            View view;
            LayoutInflater inflater = LayoutInflater.from(context);
            if (device == null) {
                view = inflater.inflate(R.layout.item_group_title, null);
                view.setEnabled(false);
            } else {
                view = inflater.inflate(R.layout.item_device_info, null);
                ((TextView) view.findViewById(R.id.item_device_name)).setText(device.getName());

                ((ImageView) view.findViewById(R.id.item_device_type)).
                        setImageDrawable(DeviceInfoActivityBak.this.getDrawable(device.getType().getDrawableId()));

                ImageView iv = view.findViewById(R.id.item_device_paired);
                View pb = view.findViewById(R.id.item_device_pairing);
                iv.setVisibility(View.GONE);
                pb.setVisibility(View.GONE);
                switch (device.getStatus()) {
                    case Paired:
                        iv.setImageDrawable(getDrawable(R.drawable.ic_check_black_24dp));
                        iv.setVisibility(View.VISIBLE);
                        break;
                    case Pairing:
                        pb.setVisibility(View.VISIBLE);
                        break;
                    case Error:
                        iv.setImageDrawable(getDrawable(R.drawable.ic_error_black_24dp));
                        iv.setVisibility(View.VISIBLE);
                        break;
                }
            }
            return view;
        }
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
        if (item.getItemId() == R.id.app_bar_write_tag) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNFCTagWriteCompleted() {
        Toast.makeText(this, R.string.tag_write_ok, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNFCTagWriteError(int strId) {
        Toast.makeText(this, strId, Toast.LENGTH_SHORT).show();
    }

}
